package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.IDataModelTable;
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
@Api(tags = {SwaggerConfig.DATA_MANAGEMENT})
@RestController
@RequestMapping("/DataManagement")
public class DataManagementController {
    @Resource
    IDataModelTable service;

    @ApiOperation("获取数据建模表数据")
    @GetMapping("/getDataModelTable/{publishStatus}")
    public ResultEntity<Object> getDataModelTable(@PathVariable("publishStatus") int publishStatus) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataModelTable(publishStatus));
    }

    @ApiOperation("获取数仓中每个表中的业务元数据配置")
    @GetMapping("/setTableRule")
    public ResultEntity<TableRuleInfoDTO> setTableRule(TableRuleParameterDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setTableRule(dto));
    }

}
