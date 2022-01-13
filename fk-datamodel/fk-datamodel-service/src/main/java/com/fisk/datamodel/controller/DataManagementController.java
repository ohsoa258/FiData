package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.IDataModelTable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_MANAGEMENT})
@RestController
@RequestMapping("/DataManagement")
public class DataManagementController {
    @Resource
    IDataModelTable service;

    @ApiOperation("获取数据建模表数据")
    @GetMapping("/getDataModelTable")
    public ResultEntity<Object> getDataModelTable() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataModelTable());
    }

}
