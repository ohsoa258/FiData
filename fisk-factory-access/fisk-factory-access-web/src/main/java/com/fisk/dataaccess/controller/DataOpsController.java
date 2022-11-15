package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.service.IDataOps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_OPS})
@RestController
@RequestMapping("/DataOps")
public class DataOpsController {

    @Resource
    IDataOps service;

    @ApiOperation("根据表名获取接入表信息")
    @GetMapping("/getTableInfo/{tableName}")
    public ResultEntity<Object> getTableInfo(@PathVariable("tableName") String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableInfo(tableName));
    }

}
