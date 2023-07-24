package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.statistics.PipelLineDetailDTO;
import com.fisk.task.vo.statistics.*;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.service.dispatchLog.IPipelLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    /**
     * getLogStatistics
     * @param lookday
     * @return
     */
    @ApiOperation("获取图表的日志统计信息")
    @GetMapping("/getLogStatistics")
    public ResultEntity<StatisticsVO> getLogStatistics(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getLogStatistics(lookday));
    }

    /**
     * getGanttChart
     * @return
     */
    @ApiOperation("获取日志统计页面甘特图")
    @GetMapping("/getGanttChart")
    public ResultEntity<List<GanttChartVO>> getGanttChart(){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getGanttChart());
    }
    /**
     * getTopRunningTime
     * @return
     */
    @ApiOperation("获取管道运行时长TOP20")
    @GetMapping("/getTopRunningTime")
    public ResultEntity<List<TopRunningTimeVO>> getTopRunningTime(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getTopRunningTime(lookday));
    }
    /**
     * getFaildStatistics
     * @return
     */
    @ApiOperation("获取失败统计图")
    @GetMapping("/getFaildStatistics")
    public ResultEntity<List<FaildStatisticsVO>> getFaildStatistics(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getFaildStatistics(lookday));
    }

    /**
     * getLineChart
     * @return
     */
    @ApiOperation("获取管道运行状态趋势图")
    @GetMapping("/getLineChart")
    public ResultEntity<List<LineChartVO>> getLineChart(@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getLineChart(lookday));
    }
    /**
     * getDetailLineChart
     * @return
     */
    @ApiOperation("获取管道运行时长TOP详情")
    @GetMapping("/getDetailLineChart")
    public ResultEntity<List<DetailLineChartVO>> getDetailLineChart(@RequestParam String workflowName,@RequestParam Integer lookday){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getDetailLineChart(workflowName,lookday));
    }

    /**
     * getPipelLineDetailLog
     * @return
     */
    @ApiOperation("获取管道日志详情页")
    @PostMapping("/getPipelLineDetailLog")
    public ResultEntity<List<PipelLineDetailVO>> getPipelLineDetailLog(@RequestBody PipelLineDetailDTO dto){
        if(dto.lookday != null){
            if (dto.lookday>30 || dto.lookday<0){
                return ResultEntityBuild.build(ResultEnum.ERROR);
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,pipelLog.getPipelLineDetailLog(dto));
    }

}
