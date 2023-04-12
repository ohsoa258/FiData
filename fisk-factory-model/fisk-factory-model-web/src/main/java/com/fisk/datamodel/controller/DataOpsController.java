package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;
import com.fisk.datamodel.service.IDataOps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/getTableInfo/{tableName}")
    public ResultEntity<DataModelTableInfoDTO> getTableInfo(@PathVariable("tableName") String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableInfo(tableName));
    }

    @ApiOperation("根据表名获取接入表信息")
    @GetMapping("/getTableColumnDisplay")
    public ResultEntity<List<String[]>> getTableColumnDisplay(@RequestParam("tableName") String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableColumnDisplay(tableName));
    }

}
