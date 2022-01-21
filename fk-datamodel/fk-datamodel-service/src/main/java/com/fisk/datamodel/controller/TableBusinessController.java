package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.ITableBusiness;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.TABLE_BUSINESS})
@RestController
@RequestMapping("/TableBusiness")
public class TableBusinessController {

    @Resource
    ITableBusiness service;

    @ApiOperation("获取表增量配置信息")
    @GetMapping("/getTableBusiness")
    public ResultEntity<Object> getTableBusiness(@RequestParam("tableId") int tableId,@RequestParam("tableType") int tableType) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableBusiness(tableId,tableType));
    }
}
