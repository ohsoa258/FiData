package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.dispatchlog.*;
import com.fisk.task.dto.query.DataServiceTableLogQueryDTO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.fisk.task.config.SwaggerConfig;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cfk
 */
@Api(tags = {SwaggerConfig.DispatchLog})
@RestController
@RequestMapping("/dispatchLog")
@Slf4j
public class DispatchLogController {
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelStageLog iPipelStageLog;
    @Resource
    IPipelLog iPipelLog;

    /**
     * 获取管道日志
     *
     * @param pipelLog pipelLog
     * @return 执行结果
     */
    @ApiOperation("获取管道日志")
    @PostMapping("/getPipelLogVos")
    public ResultEntity<List<PipelMergeLog>> getPipelLogVos(@RequestBody PipelLogVO pipelLog) {
        ResultEntity<List<PipelMergeLog>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelLog.getPipelLogVos(pipelLog);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    /**
     * getLogStatisticsForChart
     * @param pipelLog
     * @return
     */
    @ApiOperation("获取图表的日志统计信息")
    @PostMapping("/getLogStatisticsForChart")
    public ResultEntity<LogStatisticsForChartVO> getLogStatisticsForChart(@RequestBody PipelLogVO pipelLog){
        ResultEntity<LogStatisticsForChartVO> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelLog.getLogStatisticsForChart(pipelLog);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    /**
     * 获取job日志
     *
     * @param pipelJobLogs pipelJobLogs
     * @return 执行结果
     */
    @ApiOperation("获取job日志")
    @PostMapping("/getPipelJobLogVos")
    public ResultEntity<List<PipelJobMergeLogVO>> getPipelJobLogVos(@RequestBody List<PipelJobLogVO> pipelJobLogs) {
        ResultEntity<List<PipelJobMergeLogVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelJobLog.getPipelJobLogVos(pipelJobLogs);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    /**
     * 获取任务日志
     *
     * @param pipelTaskLogs pipelTaskLogs
     * @return 执行结果
     */
    @ApiOperation("获取任务日志")
    @PostMapping("/getPipelTaskLogVos")
    public ResultEntity<List<PipelTaskMergeLogVO>> getPipelTaskLogVos(@RequestBody List<PipelTaskLogVO> pipelTaskLogs) {
        ResultEntity<List<PipelTaskMergeLogVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelTaskLog.getPipelTaskLogVos(pipelTaskLogs);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    /**
     * 获取阶段日志
     *
     * @param taskId taskId
     * @return 执行结果
     */
    @ApiOperation("获取阶段日志")
    @PostMapping("/getPipelStageLogVos")
    public ResultEntity<List<PipelStageLogVO>> getPipelStageLogVos(@RequestParam String taskId) {
        ResultEntity<List<PipelStageLogVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelStageLog.getPipelStageLogVos(taskId);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    /**
     * 依据pipelTraceId查询pipelId
     * @param pipelTraceId
     * @return
     */
    @ApiOperation("依据pipelTraceId查询pipelId")
    @GetMapping("/getPipelIdByPipelTraceId")
    public ResultEntity<String> getPipelIdByPipelTraceId(@RequestParam("pipelTraceId") String pipelTraceId){
        return iPipelLog.getPipelIdByTraceId(pipelTraceId);
    }

    /**
     * 获取数据服务表服务同步日志
     *
     * @param dto
     * @return 执行结果
     */
    @ApiOperation("获取数据服务表服务同步日志")
    @PostMapping("/getDataServiceTableLogVos")
    public ResultEntity<DataServiceTableLogQueryVO> getDataServiceTableLogVos(@RequestBody DataServiceTableLogQueryDTO dto) {
        return iPipelTaskLog.getDataServiceTableLogVos(dto);
    }

    /**
     * 依据pipelTraceId查询pipelId
     * @param pipelTraceId
     * @return
     */
    @ApiOperation("依据pipelTraceId查询pipelId")
    @GetMapping("/getPipelStates")
    public ResultEntity<List<String>> getPipelStates(@RequestParam("pipelTraceId") String pipelTraceId){
        ResultEntity<List<String>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelStageLog.getPipelStates(pipelTraceId);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }




}
