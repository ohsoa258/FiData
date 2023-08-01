package com.fisk.task.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.statistics.PipelLineDetailDTO;
import com.fisk.task.dto.tableservice.TableServiceDetailDTO;
import com.fisk.task.dto.tableservice.TableServiceRecipientsDTO;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.pipeline.TableServiceRecipientsService;
import com.fisk.task.vo.statistics.*;
import com.fisk.task.vo.tableservice.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-31
 * @Description:
 */
@Api(tags = {SwaggerConfig.TABLE_SERVER_STATISTICS})
@RestController
@RequestMapping("/TableServiceStatistics")
public class TableServiceStatisticsController {

    @Resource
    TableServiceRecipientsService tableServiceRecipientsService;
    @Resource
    IPipelTaskLog pipelTaskLog;

    /**
     * 获取图表的日志统计信息
     * @param lookday
     * @return
     */
    @ApiOperation("获取图表的日志统计信息")
    @GetMapping("/getLogStatistics")
    public ResultEntity<TableStatisticsVO> getLogStatistics(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelTaskLog.getLogStatistics(lookday));
    }

    /**
     * 获取表服务日志统计页面甘特图
     * @return
     */
    @ApiOperation("获取日志统计页面甘特图")
    @GetMapping("/getGanttChart")
    public ResultEntity<List<TableGanttChartVO>> getGanttChart(){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelTaskLog.getGanttChart());
    }
    /**
     * 获取表服务运行时长TOP20
     * @param lookday
     * @return
     */
    @ApiOperation("获取表服务运行时长TOP20")
    @GetMapping("/getTopRunningTime")
    public ResultEntity<List<TableTopRunningTimeVO>> getTopRunningTime(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelTaskLog.getTopRunningTime(lookday));
    }
    /**
     * 获取表服务失败统计图
     * @return
     */
    @ApiOperation("获取表服务失败统计图")
    @GetMapping("/getFaildStatistics")
    public ResultEntity<List<TableFaildStatisticsVO>> getFaildStatistics(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelTaskLog.getFaildStatistics(lookday));
    }

    /**
     * 获取表服务运行状态趋势图
     * @return
     */
    @ApiOperation("获取表服务运行状态趋势图")
    @GetMapping("/getLineChart")
    public ResultEntity<List<TableLineChartVO>> getLineChart(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelTaskLog.getLineChart(lookday));
    }
    /**
     * 获取表服务运行时长TOP详情
     * @return
     */
    @ApiOperation("获取表服务运行时长TOP详情")
    @GetMapping("/getDetailLineChart")
    public ResultEntity<List<TableServiceLineChartVO>> getDetailLineChart(@RequestParam String workflowName, @RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelTaskLog.getDetailLineChart(workflowName,lookday));
    }

    /**
     * 获取表服务日志详情页
     * @return
     */
    @ApiOperation("获取表服务日志详情页")
    @PostMapping("/getTableServiceDetailLog")
    public ResultEntity<Page<TableServiceDetailVO>> getTableServiceDetailLog(@RequestBody TableServiceDetailDTO dto){
        if(dto.lookday != null){
            if (dto.lookday>30 || dto.lookday<0){
                return ResultEntityBuild.build(ResultEnum.ERROR);
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelTaskLog.getTableServiceDetailLog(dto));
    }

    /**
     * 查询表服务日志订阅通知配置
     * @return
     */
    @ApiOperation("查询表服务日志订阅通知配置")
    @GetMapping("/getTableServiceAlarmNotice")
    public ResultEntity<Object> getTableServiceAlarmNotice() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableServiceRecipientsService.getTableServiceAlarmNotice());
    }

    /**
     * 保存表服务日志订阅通知配置
     * @param tableServiceRecipientsDTO
     * @return
     */
    @ApiOperation("保存表服务日志订阅通知配置")
    @PostMapping("/saveTableServiceAlarmNotice")
    public ResultEntity<Object> saveTableServiceAlarmNotice(@RequestBody TableServiceRecipientsDTO tableServiceRecipientsDTO) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableServiceRecipientsService.saveTableServiceAlarmNotice(tableServiceRecipientsDTO));
    }

    /**
     * 删除表服务日志订阅通知配置
     * @return
     */
    @ApiOperation("删除表服务日志订阅通知配置")
    @GetMapping("/deleteTableServiceAlarmNotice")
    public ResultEntity<Object> deleteTableServiceAlarmNotice() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableServiceRecipientsService.deleteTableServiceAlarmNotice());
    }

    /**
     * 发送表服务日志订阅通知
     * @param content
     * @return
     */
    @ApiOperation("发送表服务日志订阅通知")
    @PostMapping("/sendTableServiceSendEmails")
    public ResultEntity<Object> sendTableServiceSendEmails(@RequestBody String content) {
        return ResultEntityBuild.build(tableServiceRecipientsService.sendTableServiceSendEmails(content));
    }
}
