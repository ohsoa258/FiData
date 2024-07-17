package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.datalogging.DataLoggingDTO;
import com.fisk.datamanagement.service.DataLoggingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-28 15:52
 * @description 数据接入的总记录数+日增量
 */
@Api(tags = {SwaggerConfig.DATA_LOGGING})
@RestController
@RequestMapping("/DataLogging")
public class DataLoggingController {
    @Resource
    DataLoggingService dataLoggingService;

    @ApiOperation("获取所有接入表的数据记录数")
    @PostMapping("/getDataTableRows")
    public ResultEntity<Object> getDataTableRows() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataLoggingService.getDataTableRows());
    }

    @ApiOperation("获取所有数据记录数")
    @PostMapping("/getDataTotals")
    public ResultEntity<Object> getDataTotals(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataLoggingService.getDataTotals());
    }

    @ApiOperation("获取管道当天运行成功失败记录数")
    @PostMapping("/getPipelTotals")
    public ResultEntity<Object> getPipelTotals(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataLoggingService.getPipelTotals());
    }

    @ApiOperation("获取管道过去7天每天的运行次数")
    @PostMapping("/getPipelWeek")
    public ResultEntity<Object> getPipelWeek() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataLoggingService.getPipelWeek());
    }
}
