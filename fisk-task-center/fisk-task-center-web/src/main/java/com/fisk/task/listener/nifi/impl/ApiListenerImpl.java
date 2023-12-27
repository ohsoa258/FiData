package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndModel.LogPageQueryDTO;
import com.fisk.common.service.accessAndModel.NifiLogResultDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.task.dto.DwLogQueryDTO;
import com.fisk.task.dto.DwLogResultDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.entity.PipelineTableLogPO;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.listener.nifi.IApiListener;
import com.fisk.task.service.nifi.IPipelineTableLog;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.IEtlLog;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author lsj
 */
@Service
@Slf4j
public class ApiListenerImpl implements IApiListener {

    @Resource
    DataAccessClient client;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    private IEtlLog iEtlLog;
    @Resource
    private IPipelineTableLog iPipelineTableLog;

    @Override
    public ResultEntity<Object> apiToStg(String data) {
        log.info("api-Java代码同步参数:{}", data);
        ResultEntity<Object> apiToStgResult = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            KafkaReceiveDTO kafkaReceive = JSON.parseObject(data, KafkaReceiveDTO.class);
            //获取topic
            String topic = kafkaReceive.topic;
            //获取大批次号
            String fidata_batch_code = kafkaReceive.fidata_batch_code;
            log.info("大批次号：{}", fidata_batch_code);

            String[] topicParameter = topic.split("\\.");
            //应用id
            String appId = "";
            //表id
            String tableId = "";
            //apiid
            long apiId;
            if (Objects.equals(topicParameter.length, 6)) {
                appId = topicParameter[4];
                tableId = topicParameter[5];
            } else if (Objects.equals(topicParameter.length, 7)) {
                appId = topicParameter[5];
                tableId = topicParameter[6];
            }

            //todo:通过物理表id,获取apiid
            ResultEntity<TableAccessDTO> result = client.getTableAccess(Integer.parseInt(tableId));
            if (ResultEnum.SUCCESS.getCode() != result.getCode()) {
                log.error("api-Java代码同步获取apiId失败 - 未获取到apiId");
                return ResultEntityBuild.build(ResultEnum.APICONFIG_ISNULL);
            }
            TableAccessDTO tableAccessDTO = result.getData();
            apiId = tableAccessDTO.getApiId();

            com.fisk.dataaccess.dto.api.ApiImportDataDTO apiImportDataDTO = new com.fisk.dataaccess.dto.api.ApiImportDataDTO();
            apiImportDataDTO.setAppId(Long.parseLong(appId));
            apiImportDataDTO.setApiId(apiId);
            apiImportDataDTO.setBatchCode(fidata_batch_code);

            //远程调用api同步数据的方法 将数据同步进stg
            apiToStgResult = client.importDataV2(apiImportDataDTO);

        } catch (Exception e) {
            log.error("api-Java代码同步报错" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEntityBuild.build(ResultEnum.API_NIFI_SYNC_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(pstmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return apiToStgResult;
    }

    /**
     * dw数仓按时间获取单表nifi日志
     *
     * @param dwLogQueryDTO
     * @return
     */
    @Override
    public DwLogResultDTO getDwTblNifiLog(DwLogQueryDTO dwLogQueryDTO) {
        TBETLlogPO tbetLlogPO = null;
        DwLogResultDTO dwLogResultDTO = new DwLogResultDTO();
        try {
            //先查询该表此次手动同步有没有报错
            LambdaQueryWrapper<PipelineTableLogPO> w = new LambdaQueryWrapper<>();
            w.eq(PipelineTableLogPO::getTableId, dwLogQueryDTO.getTblId())
                    .eq(PipelineTableLogPO::getTableType, dwLogQueryDTO.getTblType().getValue())
                    //手动调度 0   管道调度 1
                    .eq(PipelineTableLogPO::getDispatchType, 0)
                    .gt(PipelineTableLogPO::getCreateTime, dwLogQueryDTO.getPublishTime())
                    .orderByAsc(PipelineTableLogPO::getCreateTime)
                    .last("limit 1");
            PipelineTableLogPO pipelineTableLogPO = iPipelineTableLog.getOne(w);

            //如果pipelineTableLogPO为空意味着此次手动同步没有报错 则查询此次同步的数据量
            //或state == 3 意味着成功
            if (pipelineTableLogPO == null || pipelineTableLogPO.getState() == 3) {
                LambdaQueryWrapper<TBETLlogPO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TBETLlogPO::getTablename, dwLogQueryDTO.getTblName())
                        .gt(TBETLlogPO::getCreatetime, dwLogQueryDTO.getPublishTime())
                        .orderByAsc(TBETLlogPO::getCreatetime)
                        .last("limit 1");
                tbetLlogPO = iEtlLog.getOne(wrapper);
            }

            if (pipelineTableLogPO != null) {
                dwLogResultDTO.setErrorMsg(pipelineTableLogPO.getComment());
                Date startTime = pipelineTableLogPO.getStartTime();
                Date endTime = pipelineTableLogPO.getEndTime();

                if (startTime != null) {
                    dwLogResultDTO.setStartTime(startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
                if (endTime != null) {
                    dwLogResultDTO.setEndTime(endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }

                dwLogResultDTO.setDataRows(0);
                //状态；0代表正在同步，1代表同步成功，2代表同步失败
                dwLogResultDTO.setState(2);
            }

            if (tbetLlogPO != null) {
                //状态；0代表正在同步，1代表同步成功，2代表同步失败
                dwLogResultDTO.setState(tbetLlogPO.getStatus());
                dwLogResultDTO.setDataRows(tbetLlogPO.getDatarows());
                dwLogResultDTO.setStartTime(tbetLlogPO.getStartdate());
                dwLogResultDTO.setEndTime(tbetLlogPO.getEnddate());
            }
        } catch (Exception e) {
            log.error("dw数仓按时间获取单表nifi日志报错：" + e);
            throw new FkException(ResultEnum.DATA_MODEL_GET_NIFI_LOG_ERROR);
        }

        return dwLogResultDTO;
    }

    /**
     * 同步日志页面获取数接/数仓的指定表的nifi同步日志  根据表id 名称 类型
     *
     * @param dto
     * @return
     */
    @Override
    public Page<NifiLogResultDTO> getDwAndAccessTblNifiLog(LogPageQueryDTO dto) {
        List<NifiLogResultDTO> nifiLogResultDTOS = new ArrayList<>();
        //当前页
        Integer current = dto.getCurrent();
        //分页size
        Integer size = dto.getSize();

        Page<TBETLlogPO> page = new Page<>(current, size);
        try {
            LambdaQueryWrapper<TBETLlogPO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TBETLlogPO::getTablename, dto.getTableName())
                    .orderByDesc(TBETLlogPO::getCreatetime);
            page = iEtlLog.page(page, wrapper);
            for (TBETLlogPO etlLogPO : page.getRecords()) {
                NifiLogResultDTO nifiLogResultDTO = new NifiLogResultDTO();

                String topicName = etlLogPO.getTopicName();
                //如果topic和结束时间都不为空，则去tb_pipeline_table_log表判断该此同步有没有报错
                if (StringUtils.isNotEmpty(topicName) && etlLogPO.getEnddate() != null) {
                    String[] split = topicName.split("\\.");
                    int dispatchType;
                    //长度为6说明不是管道调度
                    if (split.length == 6) {
                        //手动调度 0   管道调度 1
                        dispatchType = 0;
                    } else {
                        //手动调度 0   管道调度 1
                        dispatchType = 1;
                    }
                    LocalDateTime start = etlLogPO.getStartdate();
                    LocalDateTime end = start.plusHours(3);
                    LambdaQueryWrapper<PipelineTableLogPO> w = new LambdaQueryWrapper<>();
                    w.eq(PipelineTableLogPO::getTableId, dto.getTblId())
                            .eq(PipelineTableLogPO::getTableType, dto.getTableType())
                            .eq(PipelineTableLogPO::getDispatchType, dispatchType)
                            .between(PipelineTableLogPO::getCreateTime, start, end)
                            .orderByAsc(PipelineTableLogPO::getCreateTime)
                            .last("limit 1");
                    PipelineTableLogPO pipelineTableLogPO = iPipelineTableLog.getOne(w);
                    //如果没有报错，则认为此次同步已完成并且成功（nifi未报错）
                    if (pipelineTableLogPO == null) {
                        nifiLogResultDTO.setTableName(dto.getTableName());
                        nifiLogResultDTO.setTriggerTime(etlLogPO.getStartdate());
                        nifiLogResultDTO.setTriggerType(dispatchType);
                        //已完成
                        nifiLogResultDTO.setState(1);
                        nifiLogResultDTO.setResult(etlLogPO.getStatus());
                        nifiLogResultDTO.setStartTime(etlLogPO.getStartdate());
                        nifiLogResultDTO.setEndTime(etlLogPO.getEnddate());

                        //计算持续时间
                        Duration between = Duration.between(etlLogPO.getStartdate(), etlLogPO.getEnddate());
                        String pt = between.toString().replaceFirst("PT", "");
                        nifiLogResultDTO.setDuration(pt.toLowerCase());
                        nifiLogResultDTO.setDataRows(etlLogPO.getDatarows());
                        nifiLogResultDTO.setErrorMsg("同步成功!同步数据量：" + etlLogPO.getDatarows());
                        //如果报错，则认为此次同步已完成并且失败（nifi有报错）
                    } else {
                        nifiLogResultDTO.setTableName(dto.getTableName());
                        nifiLogResultDTO.setTriggerTime(etlLogPO.getStartdate());
                        nifiLogResultDTO.setTriggerType(dispatchType);
                        //已完成
                        nifiLogResultDTO.setState(1);
                        //失败
                        nifiLogResultDTO.setResult(2);
                        nifiLogResultDTO.setStartTime(etlLogPO.getStartdate());
                        nifiLogResultDTO.setEndTime(etlLogPO.getEnddate());

                        //计算持续时间
                        Duration between = Duration.between(etlLogPO.getStartdate(), etlLogPO.getEnddate());
                        String pt = between.toString().replaceFirst("PT", "");
                        nifiLogResultDTO.setDuration(pt.toLowerCase());
                        nifiLogResultDTO.setDataRows(etlLogPO.getDatarows());
                        nifiLogResultDTO.setErrorMsg("同步失败，nifi同步流程报错!报错详情：" + pipelineTableLogPO.getComment());
                    }

                    nifiLogResultDTOS.add(nifiLogResultDTO);
                    //如果topic和结束时间都为空(或有一者为空)，则去tb_pipeline_table_log表判断该此同步现在有没有报错，如果没有则认为正在同步
                } else {
                    LocalDateTime start = etlLogPO.getStartdate();
                    LocalDateTime end = start.plusHours(3);
                    LambdaQueryWrapper<PipelineTableLogPO> w = new LambdaQueryWrapper<>();
                    w.eq(PipelineTableLogPO::getTableId, dto.getTblId())
                            .eq(PipelineTableLogPO::getTableType, dto.getTableType())
                            .between(PipelineTableLogPO::getCreateTime, start, end)
                            .orderByAsc(PipelineTableLogPO::getCreateTime)
                            .last("limit 1");
                    PipelineTableLogPO pipelineTableLogPO = iPipelineTableLog.getOne(w);
                    //如果没有报错，则认为此次同步未完成（nifi未报错）
                    if (pipelineTableLogPO == null) {
                        nifiLogResultDTO.setTableName(dto.getTableName());
                        nifiLogResultDTO.setTriggerTime(etlLogPO.getStartdate());
                        //-1正在同步  未结束时表内无法判断是手动还是管道调度
                        nifiLogResultDTO.setTriggerType(-1);
                        //0 进行中
                        nifiLogResultDTO.setState(0);
                        //0 正在同步
                        nifiLogResultDTO.setResult(0);
                        LocalDateTime startDate = etlLogPO.getStartdate();
                        LocalDateTime now = LocalDateTime.now();

                        Duration duration = Duration.between(startDate, now);

                        //处理错误日志  大于一天的认为失败
                        if (duration.toDays() > 1) {
                            nifiLogResultDTO.setTriggerType(0);
                            //1 已完成
                            nifiLogResultDTO.setState(1);
                            //2 同步失败
                            nifiLogResultDTO.setResult(2);
                            // 距离当前时间大于一天
                            nifiLogResultDTO.setStartTime(startDate);
                            nifiLogResultDTO.setEndTime(etlLogPO.getEnddate());

                            //计算持续时间
                            nifiLogResultDTO.setDuration("同步失败");
                            nifiLogResultDTO.setDataRows(etlLogPO.getDatarows());
                            nifiLogResultDTO.setErrorMsg("本次同步失败:同步超时...");
                        } else {
                            // 距离当前时间不大于一天
                            nifiLogResultDTO.setStartTime(startDate);
                            nifiLogResultDTO.setEndTime(etlLogPO.getEnddate());

                            //计算持续时间
                            nifiLogResultDTO.setDuration("同步中");
                            nifiLogResultDTO.setDataRows(etlLogPO.getDatarows());
                            nifiLogResultDTO.setErrorMsg("NIFI正在同步中...");
                        }

                        //如果报错，则认为此次同步已完成并且失败（nifi有报错）
                    } else {
                        nifiLogResultDTO.setTableName(dto.getTableName());
                        nifiLogResultDTO.setTriggerTime(etlLogPO.getStartdate());
                        nifiLogResultDTO.setTriggerType(pipelineTableLogPO.getDispatchType());
                        //已完成
                        nifiLogResultDTO.setState(1);
                        //失败
                        nifiLogResultDTO.setResult(2);
                        nifiLogResultDTO.setStartTime(etlLogPO.getStartdate());
                        nifiLogResultDTO.setEndTime(pipelineTableLogPO.getCreateTime());

                        //计算持续时间
                        Duration between = Duration.between(etlLogPO.getStartdate(), pipelineTableLogPO.getCreateTime());
                        String pt = between.toString().replaceFirst("PT", "");
                        nifiLogResultDTO.setDuration(pt.toLowerCase());
                        nifiLogResultDTO.setDataRows(etlLogPO.getDatarows());
                        nifiLogResultDTO.setErrorMsg("同步失败，nifi同步流程报错!报错详情：" + pipelineTableLogPO.getComment());
                    }
                    nifiLogResultDTOS.add(nifiLogResultDTO);
                }
            }
        } catch (Exception e) {
            log.error("同步日志页面获取数接/数仓的指定表的nifi同步日志报错：" + e);
            throw new FkException(ResultEnum.GET_NIFI_LOG_ERROR);
        }
        Page<NifiLogResultDTO> resultDTOPage = new Page<>();
        resultDTOPage.setTotal(page.getTotal());
        resultDTOPage.setRecords(nifiLogResultDTOS);
        resultDTOPage.setCurrent(current);
        resultDTOPage.setSize(size);
        resultDTOPage.setOrders(page.getOrders());
        return resultDTOPage;
    }

}
