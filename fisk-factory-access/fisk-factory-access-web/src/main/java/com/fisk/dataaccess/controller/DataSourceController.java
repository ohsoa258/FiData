package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.service.IAppDataSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "根据appId获取所有数据源以及数据库、表数据")
    @GetMapping("/getDataSourceMeta/{appId}")
    public ResultEntity<Object> getDataSourceMeta(@PathVariable long appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourceMeta(appId));
    }

    @ApiOperation(value = "根据appId重新加载所有数据源以及数据库、表数据")
    @GetMapping("/setDataSourceMeta/{appId}")
    public ResultEntity<Object> setDataSourceMeta(@PathVariable long appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setDataSourceMeta(appId));
    }

    @PostMapping("/getDatabaseNameList")
    @ApiOperation(value = "根据服务配置信息,获取所有的数据库名称")
    public ResultEntity<Object> getDatabaseNameList(@RequestBody AppDataSourceDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDatabaseNameList(dto));
    }
}
