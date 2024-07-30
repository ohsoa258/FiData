package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.enums.HttpRequestEnum;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryQueryDTO;
import com.fisk.datamodel.entity.TableHistoryPO;
import com.fisk.datamodel.map.TableHistoryMap;
import com.fisk.datamodel.mapper.TableHistoryMapper;
import com.fisk.datamodel.service.IDimension;
import com.fisk.datamodel.service.IFact;
import com.fisk.datamodel.service.ITableHistory;
import com.fisk.datamodel.utils.httprequest.ApiHttpRequestFactoryHelper;
import com.fisk.datamodel.utils.httprequest.IBuildHttpRequest;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.DwLogQueryDTO;
import com.fisk.task.dto.DwLogResultDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class TableHistoryImpl
        extends ServiceImpl<TableHistoryMapper, TableHistoryPO>
        implements ITableHistory {

    @Resource
    TableHistoryMapper mapper;

    @Resource
    private IFact Ifact;

    @Resource
    private IDimension Idimension;

    @Resource
    private PublishTaskClient taskClient;

    @Override
    public ResultEnum addTableHistory(List<TableHistoryDTO> dto) {
        dto.stream().filter(Objects::nonNull)
                .forEach(e -> {
                    if (e.openTransmission) {
                        e.remark = e.remark + " --> 同步";
                    } else {
                        e.remark = e.remark + " --> 未同步";
                    }
                });
        return this.saveBatch(TableHistoryMap.INSTANCES.dtoListToPoList(dto)) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<TableHistoryDTO> getTableHistoryList(TableHistoryQueryDTO dto) {
        QueryWrapper<TableHistoryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableHistoryPO::getTableId, dto.tableId)
                .eq(TableHistoryPO::getTableType, dto.tableType);
        List<TableHistoryDTO> tableHistoryDTOS = TableHistoryMap.INSTANCES.poListToDtoList(mapper.selectList(queryWrapper));

        for (TableHistoryDTO tableHistoryDTO : tableHistoryDTOS) {

            int tableType = tableHistoryDTO.tableType;
            int tableId = tableHistoryDTO.getTableId();
            LocalDateTime createTime = tableHistoryDTO.getCreateTime();
            TableHistoryQueryDTO tableHistoryQueryDTO = new TableHistoryQueryDTO();

            tableHistoryQueryDTO.setTableId(tableId);
            tableHistoryQueryDTO.setTableType(tableType);
            tableHistoryQueryDTO.setPublishTime(createTime);
            DwLogResultDTO dwPublishNifiStatus = getDwPublishNifiStatus(tableHistoryQueryDTO);
            tableHistoryDTO.setDto(dwPublishNifiStatus);
        }
        return tableHistoryDTOS;
    }

    /**
     * 获取数仓表单表发布时，nifi的同步情况：日志+报错信息
     *
     * @param dto
     * @return
     */
    @Override
    public DwLogResultDTO getDwPublishNifiStatus(TableHistoryQueryDTO dto) {
        DwLogQueryDTO dwLogQueryDTO = new DwLogQueryDTO();
        try {
            //表id
            int tableId = dto.getTableId();
            //表类别：0维度 1事实
            int tableType = dto.getTableType();
            //获取发布时间
            LocalDateTime publishTime = dto.getPublishTime();
            DimensionDTO dimension;
            FactDTO fact;
            //表名
            String tblName;
            //表类别
            OlapTableEnum anEnum;
            //维度  分别获取维度/事实表的详情  表名等
            if (tableType == 0) {
                dimension = Idimension.getDimension(tableId);
                tblName = dimension.dimensionTabName;
                anEnum = OlapTableEnum.DIMENSION;
            } else {
                fact = Ifact.getFact(tableId);
                tblName = fact.factTabName;
                anEnum = OlapTableEnum.FACT;
            }

            dwLogQueryDTO.setTblId(tableId);
            dwLogQueryDTO.setTblName(tblName);
            dwLogQueryDTO.setTblType(anEnum);
            dwLogQueryDTO.setPublishTime(publishTime);
            DwLogResultDTO dwTblNifiLog = taskClient.getDwTblNifiLog(dwLogQueryDTO);

            if (!StringUtils.isEmpty(dwTblNifiLog.getErrorMsg())) {
                String errorMsg = dwTblNifiLog.getErrorMsg();
                //截取doris严格模式报错时，给出的追踪地址
                if (errorMsg.contains("tracking_url=")) {
                    errorMsg = replaceUrlToDetail(errorMsg);
                    dwTblNifiLog.setErrorMsg(errorMsg);
                }
            }

            return dwTblNifiLog;
        } catch (Exception e) {
            log.error("数仓建模-获取nifi同步日志报错：" + e);
            throw new FkException(ResultEnum.DATA_MODEL_GET_NIFI_LOG_ERROR);
        }
    }

    /**
     * 将doris严格模式 报错信息中的追踪地址替换成具体的报错原因
     *
     * @param errorMsg
     * @return
     */
    public static String replaceUrlToDetail(String errorMsg) {
        //地址
        String dorisErrorUrl = errorMsg.substring(errorMsg.lastIndexOf("tracking_url=") + 13, errorMsg.lastIndexOf(";"));
        log.info("tracking_url = ：" + dorisErrorUrl);
        //发送请求，获取doris因严格模式报错时，具体的原因（什么字段的什么内容导致的报错）
        ApiHttpRequestDTO apiHttpRequestDTO = new ApiHttpRequestDTO();
        apiHttpRequestDTO.setUri(dorisErrorUrl);
        apiHttpRequestDTO.setHttpRequestEnum(HttpRequestEnum.GET);
        IBuildHttpRequest iBuildHttpRequest = ApiHttpRequestFactoryHelper.buildHttpRequest(apiHttpRequestDTO);
        String s = iBuildHttpRequest.httpRequest(apiHttpRequestDTO);
        return errorMsg + "详细原因：" + s;
    }

}
