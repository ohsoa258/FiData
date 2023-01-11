package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.logs.LogQueryBasicsDTO;
import com.fisk.dataservice.dto.logs.LogQueryDTO;
import com.fisk.dataservice.entity.AppServiceConfigPO;
import com.fisk.dataservice.entity.LogPO;
import com.fisk.dataservice.entity.TableServicePO;
import com.fisk.dataservice.mapper.AppServiceConfigMapper;
import com.fisk.dataservice.mapper.LogsMapper;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.ILogsManageService;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import com.fisk.dataservice.vo.logs.TableServiceLogDetailVO;
import com.fisk.dataservice.vo.logs.TableServiceLogVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.dispatchlog.DataServiceTableLogQueryVO;
import com.fisk.task.dto.dispatchlog.DataServiceTableLogVO;
import com.fisk.task.dto.query.DataServiceTableLogQueryDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 日志接口实现类
 * @date 2022/3/7 12:36
 */
@Service
@Slf4j
public class LogsManageImpl extends ServiceImpl<LogsMapper, LogPO> implements ILogsManageService {
    @Resource
    private TableServiceMapper tableServiceMapper;

    @Resource
    private AppServiceConfigMapper appServiceConfigMapper;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Override
    public Page<ApiLogVO> pageFilter(LogQueryDTO query) {
        return baseMapper.filter(query.page, query.apiId, query.appId, query.keyword);
    }

    @Async
    @Override
    public void saveLog(LogPO po) {
        baseMapper.insert(po);
    }

    @Override
    public ResultEntity<TableServiceLogVO> pageTableServiceLog(LogQueryBasicsDTO dto) {
        TableServiceLogVO tableServiceLogVO = new TableServiceLogVO();
        try {
            if (dto.getAppId() == 0) {
                return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL, null);
            }
            // 第一步：判断表服务ID是否为空，为空则查询应用下所有的表服务ID
            List<Integer> serviceIdList = new ArrayList<>();
            if (dto.getTableServiceId() == 0) {
                QueryWrapper<AppServiceConfigPO> appServiceConfigPOQueryWrapper = new QueryWrapper<>();
                appServiceConfigPOQueryWrapper
                        .lambda()
                        .eq(AppServiceConfigPO::getDelFlag, 1)
                        .eq(AppServiceConfigPO::getAppId, dto.getAppId())
                        .eq(AppServiceConfigPO::getType, 2);
                List<AppServiceConfigPO> appServiceConfigPOS = appServiceConfigMapper.selectList(appServiceConfigPOQueryWrapper);
                if (CollectionUtils.isNotEmpty(appServiceConfigPOS)) {
                    serviceIdList = appServiceConfigPOS.stream().map(AppServiceConfigPO::getServiceId).collect(Collectors.toList());
                }
            } else {
                serviceIdList.add(dto.getTableServiceId());
            }
            if (CollectionUtils.isEmpty(serviceIdList)) {
                return ResultEntityBuild.build(ResultEnum.SUCCESS, tableServiceLogVO);
            }
            // 第二步：根据表服务ID查询表服务配置详情
            QueryWrapper<TableServicePO> tableServicePOQueryWrapper = new QueryWrapper<>();
            tableServicePOQueryWrapper
                    .lambda()
                    .eq(TableServicePO::getDelFlag, 1)
                    .in(TableServicePO::getId, serviceIdList);
            List<TableServicePO> tableServicePOS = tableServiceMapper.selectList(tableServicePOQueryWrapper);
            if (CollectionUtils.isEmpty(tableServicePOS)) {
                return ResultEntityBuild.build(ResultEnum.SUCCESS, tableServiceLogVO);
            }
            HashMap<Long, String> tableList = new HashMap<>();
            for (int i = 0; i < tableServicePOS.size(); i++) {
                tableList.put(tableServicePOS.get(i).getId(), tableServicePOS.get(i).getDisplayName());
            }
            // 第三步：查询Task表服务日志同步详情
            DataServiceTableLogQueryDTO queryDTO = new DataServiceTableLogQueryDTO();
            queryDTO.setCurrent(dto.getCurrent());
            queryDTO.setSize(dto.getSize());
            queryDTO.setTableList(tableList);
            queryDTO.setTableType(OlapTableEnum.DATASERVICES.getValue());
            queryDTO.setKeyword(dto.getKeyword());
            ResultEntity<DataServiceTableLogQueryVO> dataServiceTableLogVos = publishTaskClient.getDataServiceTableLogVos(queryDTO);
            if (dataServiceTableLogVos == null
                    || dataServiceTableLogVos.getCode() != ResultEnum.SUCCESS.getCode()
                    || dataServiceTableLogVos.getData() == null
                    || CollectionUtils.isEmpty(dataServiceTableLogVos.getData().getDataArray())) {
                ResultEnum resultEnum = ResultEnum.getEnum(dataServiceTableLogVos.getCode());
                return ResultEntityBuild.build(resultEnum, tableServiceLogVO);
            }
            // 第四步：结果赋值给页面VO对象
            DataServiceTableLogQueryVO data = dataServiceTableLogVos.getData();
            List<DataServiceTableLogVO> dataArray = data.getDataArray();

            tableServiceLogVO.setCurrent(data.getCurrent());
            tableServiceLogVO.setSize(data.getSize());
            tableServiceLogVO.setPage(data.getPage());
            tableServiceLogVO.setTotal(data.getTotal());
            List<TableServiceLogDetailVO> logList = new ArrayList<>();
            dataArray.forEach(t -> {
                TableServiceLogDetailVO log = new TableServiceLogDetailVO();
                log.setTableId(t.getTableId());
                log.setTaskTraceId(t.getTaskTraceId());
                log.setTableDisplayName(t.getTableDisplayName());
                log.setStartTime(t.getStartTime());
                log.setEndTime(t.getEndTime());
                log.setMsg(t.getMsg());
                logList.add(log);
            });
            tableServiceLogVO.setDataArray(logList);
        } catch (Exception ex) {
            log.error("【pageTableServiceLog】分页查询表服务同步日志异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【pageTableServiceLog】 ex：" + ex);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableServiceLogVO);
    }
}
