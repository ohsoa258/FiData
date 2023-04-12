package com.fisk.datafactory.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.config.SwaggerConfig;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.SYSTEM_WEB_INDEX})
@RestController
@RequestMapping("/systemWebIndex")
public class SystemWebIndexController {

    @Resource
    INifiCustomWorkflow service;

    @ApiOperation(value = "查询数据调度图当天运行情况")
    @GetMapping("/getDataSchedule")
    public ResultEntity<Object> getDataAccessNum() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getNum());
    }

    @ApiOperation(value = "查询数据调度应用总个数")
    @GetMapping("/getDataDispatchNum")
    public ResultEntity<Object> getDataDispatchNum(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataDispatchNum());
    }
}
