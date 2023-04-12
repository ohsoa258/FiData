package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_LogsQueryDTO;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleDTO;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleQueryDTO;
import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_WhiteListDTO;
import com.fisk.datagovernance.service.datasecurity.IIntelligentDiscovery_RuleManageService;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据安全-智能发现
 * @date 2022/3/22 16:14
 */
@Api(tags = {SwaggerConfig.INTELLIGENT_DISCOVERY})
@RestController
@RequestMapping("/intelligentdiscovery")
public class IntelligentDiscoveryController {
    @Resource
    private IIntelligentDiscovery_RuleManageService service;

    @GetMapping("/getSearchColumn")
    @ApiOperation(value = "获取搜索条件字段")
    public ResultEntity<Object> getSearchColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSearchColumn());
    }

    @PostMapping("/getRulePageList")
    @ApiOperation("获取智能发现规则分页列表")
    public ResultEntity<Page<IntelligentDiscovery_RuleVO>> getRulePageList(@RequestBody IntelligentDiscovery_RuleQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRulePageList(dto));
    }

    @PostMapping("/addRule")
    @ApiOperation("添加智能发现规则")
    public ResultEntity<Object> addRule(@RequestBody IntelligentDiscovery_RuleDTO dto) {
        return ResultEntityBuild.build(service.addRule(dto));
    }

    @PutMapping("/editRule")
    @ApiOperation("编辑智能发现规则")
    public ResultEntity<Object> editRule(@RequestBody IntelligentDiscovery_RuleDTO dto) {
        return ResultEntityBuild.build(service.editRule(dto));
    }

    @ApiOperation("启用/禁用智能发现规则")
    @PutMapping("/editRuleState/{id}")
    public ResultEntity<Object> editRuleState(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.editRuleState(id));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除智能发现规则")
    public ResultEntity<Object> deleteRule(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteRule(id));
    }

    @ApiOperation("智能发现规则扫描日志分页列表")
    @PostMapping("/getRuleScanLogPageList")
    public ResultEntity<Page<IntelligentDiscovery_LogsVO>> getRuleScanLogPageList(@RequestBody IntelligentDiscovery_LogsQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRuleScanLogPageList(dto));
    }

    @ApiOperation("下载智能发现规则扫描结果")
    @GetMapping("/downloadRuleScanResult/{uniqueId}")
    @ControllerAOPConfig(printParams = false)
    public void downloadRuleScanResult(@PathVariable("uniqueId") String uniqueId, HttpServletResponse response) {
        service.downloadRuleScanResult(uniqueId, response);
    }

    @ApiOperation("预览智能发现规则扫描结果")
    @PostMapping("/previewScanResult")
    public ResultEntity<IntelligentDiscovery_ScanResultVO> previewScanResult(@RequestParam("absolutePath") String absolutePath) {
        return service.previewScanResult(absolutePath);
    }

    @ApiOperation("获取智能发现规则扩展信息")
    @PostMapping("/getRuleExtInfo")
    public ResultEntity<IntelligentDiscovery_RuleExtInfoVO> getRuleExtInfo() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRuleExtInfo());
    }

    @ApiOperation("获取Cron表达式最近3次执行时间")
    @GetMapping("/getNextCronExeTime/{cron}")
    public ResultEntity<List<String>> getNextCronExeTime(@PathVariable("cron") String cron) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getNextCronExeTime(cron));
    }

    @ApiOperation("执行智能发现规则扫描并发送风险数据")
    @GetMapping("/createScanReport")
    public ResultEntity<Object> createScanReport(@RequestParam("id") int id) {
        return service.createScanReport(id);
    }

    @ApiOperation("智能发现规则扫描风险项移入/移出白名单")
    @PostMapping("/saveWhiteList")
    public ResultEntity<Object> saveWhiteList(@RequestBody IntelligentDiscovery_WhiteListDTO dto) {
        return ResultEntityBuild.build(service.saveWhiteList(dto));
    }
}
