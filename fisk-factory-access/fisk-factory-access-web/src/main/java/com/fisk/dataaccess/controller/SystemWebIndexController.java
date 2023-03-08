package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.service.IAppRegistration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_8})
@RestController
@RequestMapping("/systemWebIndex")
public class SystemWebIndexController {

    @Resource
    IAppRegistration service;

    @ApiOperation(value = "查询数据接入下所有业务系统个数")
    @GetMapping("/getApp")
    public ResultEntity<Object> getDataAccessNum() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataAccessNum());
    }

    @ApiOperation("数据接入同步类型表个数统计")
    @GetMapping("/getSyncTableCount")
    public ResultEntity<Object> getSyncTableCount(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSyncTableCount());
    }
}
