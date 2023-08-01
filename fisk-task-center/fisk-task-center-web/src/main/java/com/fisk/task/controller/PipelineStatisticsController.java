package com.fisk.task.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.monitor.MonitorRecipientsDTO;
import com.fisk.task.dto.statistics.PipelLineDetailDTO;
import com.fisk.task.dto.statistics.PipelLogRecipientsDTO;
import com.fisk.task.service.pipeline.PipelLogRecipientsService;
import com.fisk.task.vo.statistics.*;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.service.dispatchLog.IPipelLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2023-07-18
 * @Description:
 */
@Api(tags = {SwaggerConfig.PIPELINE_STATISTICS})
@RestController
@RequestMapping("/PipelineStatistics")
public class PipelineStatisticsController {

    @Resource
    IPipelLog pipelLog;

    @Resource
    PipelLogRecipientsService pipelLogRecipientsService;
    /**
     * 获取图表的日志统计信息
     * @param lookday
     * @return
     */
    @ApiOperation("获取图表的日志统计信息")
    @GetMapping("/getLogStatistics")
    public ResultEntity<StatisticsVO> getLogStatistics(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getLogStatistics(lookday));
    }

    /**
     * 获取日志统计页面甘特图
     * @return
     */
    @ApiOperation("获取日志统计页面甘特图")
    @GetMapping("/getGanttChart")
    public ResultEntity<List<GanttChartVO>> getGanttChart(){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getGanttChart());
    }
    /**
     * 获取管道运行时长TOP20
     * @param lookday
     * @return
     */
    @ApiOperation("获取管道运行时长TOP20")
    @GetMapping("/getTopRunningTime")
    public ResultEntity<List<TopRunningTimeVO>> getTopRunningTime(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getTopRunningTime(lookday));
    }
    /**
     * 获取失败统计图
     * @param lookday
     * @return
     */
    @ApiOperation("获取失败统计图")
    @GetMapping("/getFaildStatistics")
    public ResultEntity<List<FaildStatisticsVO>> getFaildStatistics(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getFaildStatistics(lookday));
    }

    /**
     * 获取管道运行状态趋势图
     * @param lookday
     * @return
     */
    @ApiOperation("获取管道运行状态趋势图")
    @GetMapping("/getLineChart")
    public ResultEntity<List<LineChartVO>> getLineChart(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getLineChart(lookday));
    }

    /**
     * 获取管道运行时长TOP详情
     * @param workflowName
     * @param lookday
     * @return
     */
    @ApiOperation("获取管道运行时长TOP详情")
    @GetMapping("/getDetailLineChart")
    public ResultEntity<List<DetailLineChartVO>> getDetailLineChart(@RequestParam String workflowName,@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getDetailLineChart(workflowName,lookday));
    }

    /**
     * 获取管道日志详情页
     * @param dto
     * @return
     */
    @ApiOperation("获取管道日志详情页")
    @PostMapping("/getPipelLineDetailLog")
    public ResultEntity<Page<PipelLineDetailVO>> getPipelLineDetailLog(@RequestBody PipelLineDetailDTO dto){
        if(dto.lookday != null){
            if (dto.lookday>30 || dto.lookday<0){
                return ResultEntityBuild.build(ResultEnum.ERROR);
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getPipelLineDetailLog(dto));
    }
    /**
     * 查询管道日志订阅通知配置
     * @return
     */
    @ApiOperation("查询管道日志订阅通知配置")
    @GetMapping("/getPipelLogAlarmNotice")
    public ResultEntity<Object> getPipelLogAlarmNotice() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, pipelLogRecipientsService.getPipelLogAlarmNotice());
    }
    /**
     * 保存管道日志订阅通知配置
     * @param pipelLogRecipientsDTO
     * @return
     */
    @ApiOperation("保存管道日志订阅通知配置")
    @PostMapping("/savePipelLogAlarmNotice")
    public ResultEntity<Object> savePipelLogAlarmNotice(@RequestBody PipelLogRecipientsDTO pipelLogRecipientsDTO) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, pipelLogRecipientsService.savePipelLogAlarmNotice(pipelLogRecipientsDTO));
    }
    /**
     * 删除管道日志订阅通知配置
     * @return
     */
    @ApiOperation("删除管道日志订阅通知配置")
    @GetMapping("/deletePipelLogAlarmNotice")
    public ResultEntity<Object> deletePipelLogAlarmNotice() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, pipelLogRecipientsService.deletePipelLogAlarmNotice());
    }
    /**
     * 发送管道日志订阅通知
     * @param content
     * @return
     */
    @ApiOperation("发送管道日志订阅通知")
    @PostMapping("/sendPipelLogSendEmails")
    public ResultEntity<Object> sendPipelLogSendEmails(@RequestBody String content) {
        return ResultEntityBuild.build(pipelLogRecipientsService.sendPipelLogSendEmails(content));
    }
}
