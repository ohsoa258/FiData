package com.fisk.dataaccess.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.service.ITableAccess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/dataAccessTree")
public class DataAccessController {

    @Resource
    private ITableAccess service;

    @ApiOperation("应用注册tree")
    @GetMapping("/getTree")
    public ResultEntity<Object> getTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTree());
    }

}
