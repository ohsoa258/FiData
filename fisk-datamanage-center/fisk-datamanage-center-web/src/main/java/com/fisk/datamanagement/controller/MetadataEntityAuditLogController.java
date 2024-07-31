package com.fisk.datamanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.assetschangeanalysis.AssetsChangeAnalysisDTO;
import com.fisk.datamanagement.dto.assetschangeanalysis.AssetsChangeAnalysisDetailDTO;
import com.fisk.datamanagement.dto.assetschangeanalysis.AssetsChangeAnalysisDetailQueryDTO;
import com.fisk.datamanagement.dto.assetschangeanalysis.AssetsChangeAnalysisQueryDTO;
import com.fisk.datamanagement.service.IMetadataEntityAuditLog;
import com.fisk.datamanagement.service.impl.MetaAnalysisEmailConfigServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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

    @Resource
    private MetaAnalysisEmailConfigServiceImpl metaAnalysisEmailConfigService;

    @ApiOperation("获取元数据审计日志")
    @GetMapping("/get")
    public ResultEntity<Object> get(Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetadataAuditLog(entityId));
    }

    @ApiOperation("获取元数据审计分析汇总")
    @GetMapping("/analysis/allChangeTotal")
    public ResultEntity<Object> analysisAllChangeTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.analysisAllChangeTotal());
    }

    @ApiOperation("获取元数据审计分析每日汇总")
    @GetMapping("/analysis/dayChangeTotal")
    public ResultEntity<Object> analysisDayChangeTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.analysisDayChangeTotal());
    }

    /**
     * 获取元数据变更影响分析首页图表信息
     *
     * @return
     */
    @ApiOperation("获取元数据变更影响分析首页图表信息")
    @PostMapping("/getMetaChangesCharts")
    public ResultEntity<AssetsChangeAnalysisDTO> getMetaChangesCharts(@RequestBody AssetsChangeAnalysisQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetaChangesCharts(dto));
    }


    /**
     * 获取元数据变更影响分析
     *
     * @return
     */
    @ApiOperation("获取元数据变更影响分析")
    @PostMapping("/getMetaChangesChartsDetail")
    public ResultEntity<Page<AssetsChangeAnalysisDetailDTO>> getMetaChangesChartsDetail(@RequestBody AssetsChangeAnalysisDetailQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetaChangesChartsDetail(dto));
    }

    /**
     * 立即发送邮件
     *
     * @return
     */
    @ApiOperation("立即发送邮件")
    @PostMapping("/sendEmailNow")
    public ResultEntity<Object> sendEmailNow() {
        metaAnalysisEmailConfigService.sendEmailOfMetaAudit();
        return ResultEntityBuild.build(ResultEnum.SUCCESS);

    }


}
