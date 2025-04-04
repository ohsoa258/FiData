package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckRulePageDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.*;
import com.fisk.datagovernance.service.dataquality.IQualityReportManageService;
import com.fisk.datagovernance.vo.dataquality.qualityreport.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告
 * @date 2022/3/22 16:15
 */
@Api(tags = {SwaggerConfig.QUALITY_REPORT_CONTROLLER})
@RestController
@RequestMapping("/QualityReport")
public class QualityReportController {

    @Resource
    private IQualityReportManageService service;

    @ApiOperation("质量报告分页")
    @PostMapping("/page")
    public ResultEntity<Page<QualityReportVO>> getAll(@RequestBody QualityReportQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation(value = "质量报告筛选器")
    @GetMapping("/getColumn")
    public ResultEntity<Object> getColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }

    @ApiOperation("质量报告新增")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody QualityReportDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("质量报告编辑")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody QualityReportEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("质量报告启用/禁用")
    @PutMapping("/editState/{id}")
    public ResultEntity<Object> editState(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.editState(id));
    }

    @ApiOperation("质量报告删除")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    @ApiOperation("执行质量报告")
    @PostMapping("/collReport")
    public ResultEntity<Object> collReport(@RequestParam("id") int id) {
        return ResultEntityBuild.build(service.collReport(id));
    }

    @ApiOperation("获取创建质量报告所需的扩展信息（邮件服务器、用户邮箱）")
    @GetMapping("/getMailServerAndUserInfo")
    public ResultEntity<QualityReportExtVO> getMailServerAndUserInfo() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMailServerAndUserInfo());
    }

    @ApiOperation("获取质量报告数据校验规则弹窗列表")
    @PostMapping("/getQualityReportRuleList")
    public ResultEntity<Page<QualityReportExt_RuleVO>> getQualityReportRuleList(@RequestBody DataCheckRulePageDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getQualityReportRuleList(dto));
    }

    @ApiOperation("数据校验质量报告日志分页")
    @PostMapping("/getAllReportLog")
    public ResultEntity<Page<QualityReportLogVO>> getAllReportLog(@RequestBody QualityReportLogQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataCheckQualityReportLog(dto));
    }

    @ApiOperation("下载报告记录")
    @GetMapping("/downloadReportRecord/{reportLogId}")
    @ControllerAOPConfig(printParams = false)
    public void downloadReportRecord(@PathVariable("reportLogId") int reportLogId, HttpServletResponse response) {
        service.downloadReportRecord(reportLogId, response);
    }

    @ApiOperation("下载Excel附件")
    @GetMapping("/downloadExcelReport/{attachmentId}")
    @ControllerAOPConfig(printParams = false)
    public void downloadExcelReport(@PathVariable("attachmentId") int attachmentId, HttpServletResponse response) {
        service.downloadExcelReport(attachmentId, response);
    }

    @ApiOperation("预览报告记录")
    @GetMapping("/previewReportRecord/{reportLogId}")
    public ResultEntity<List<PreviewQualityReportVO>> previewReportRecord(@PathVariable("reportLogId") int reportLogId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.previewReportRecord(reportLogId));
    }

    @ApiOperation("获取Cron表达式最近3次执行时间")
    @GetMapping("/getNextCronExeTime/{cron}")
    public ResultEntity<List<String>> getNextCronExeTime(@PathVariable("cron") String cron) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getNextCronExeTime(cron));
    }
}
