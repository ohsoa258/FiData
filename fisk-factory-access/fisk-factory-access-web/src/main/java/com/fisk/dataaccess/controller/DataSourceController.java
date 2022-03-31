package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.service.IAppDataSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_7})
@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Resource
    IAppDataSource service;

    @ApiOperation(value = "获取所有数据源以及数据库、表数据")
    @GetMapping("/getDataSourceMeta/{appId}")
    public ResultEntity<Object> getDataSourceMeta(@PathVariable long appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourceMeta(appId));
    }
}
