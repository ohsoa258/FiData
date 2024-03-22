package com.fisk.datamanagement.controller;

import com.azure.core.annotation.Get;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.service.IMetadataEntityAuditLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JinXingWang
 */
@Api(tags = {SwaggerConfig.METADATA_AUDIT})
@RestController
@RequestMapping("/MetadataAudit")
public class MetadataEntityAuditLogController {
    @Resource
    IMetadataEntityAuditLog service;

    @ApiOperation("获取元数据审计日志")
    @GetMapping("/get")
    public ResultEntity<Object> get(Integer entityId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetadataAuditLog(entityId));
    }

    @ApiOperation("获取元数据审计分析汇总")
    @GetMapping("/analysis/allChangeTotal")
    public ResultEntity<Object> analysisAllChangeTotal(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.analysisAllChangeTotal());
    }

    @ApiOperation("获取元数据审计分析每日汇总")
    @GetMapping("/analysis/dayChangeTotal")
    public ResultEntity<Object> analysisDayChangeTotal(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.analysisDayChangeTotal());
    }
}
