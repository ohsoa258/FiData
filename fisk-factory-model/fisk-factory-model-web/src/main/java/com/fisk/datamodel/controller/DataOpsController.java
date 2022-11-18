package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;
import com.fisk.datamodel.service.IDataOps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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
    @PostMapping("/getTableInfo")
    public ResultEntity<DataModelTableInfoDTO> getTableInfo(@Validated @RequestBody String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableInfo(tableName));
    }

    @ApiOperation("根据表名获取接入表信息")
    @PostMapping("/getTableColumnDisplay")
    public ResultEntity<List<String[]>> getTableColumnDisplay(@Validated @RequestBody String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableColumnDisplay(tableName));
    }

}
