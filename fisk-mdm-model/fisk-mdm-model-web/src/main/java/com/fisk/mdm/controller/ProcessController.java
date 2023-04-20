package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.process.*;
import com.fisk.mdm.service.ProcessService;
import com.fisk.mdm.vo.process.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Api(tags = {SwaggerConfig.TAG_13})
@RestController
@RequestMapping("/process")
public class ProcessController {

    @Resource
    ProcessService processService;

    @ApiOperation("获取流程实体")
    @GetMapping("/getProcessEntity")
    public ResultEntity<ProcessInfoVO> getProcessEntity(Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getProcess(entityId));
    }

    @ApiOperation("保存流程实体")
    @PostMapping("/saveProcessEntity")
    public ResultEntity<ResultEnum> saveProcessEntity(@RequestBody ProcessInfoDTO dto) {
        return ResultEntityBuild.build(processService.saveProcess(dto));
    }
    @ApiOperation("校验是否需要走流程")
    @GetMapping("/verifyProcessApply")
    public ResultEntity<ResultEnum> verifyProcessApply(Integer entityId) {
        return ResultEntityBuild.build(processService.verifyProcessApply(entityId));
    }

    @ApiOperation("获取我的申请")
    @PostMapping("/getMyProcessApply")
    public ResultEntity<Page<ProcessApplyVO>> getMyProcessApply(@RequestBody ProcessApplyDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getMyProcessApply(dto));
    }

    @ApiOperation("获取待处理审批列表")
    @PostMapping("/getPendingApproval")
    public ResultEntity<Page<PendingApprovalVO>> getPendingApproval(@RequestBody PendingApprovalDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getPendingApproval(dto));
    }

    @ApiOperation("获取所有审批列表")
    @PostMapping("/getAllApproval")
    public ResultEntity<Page<AllApprovalVO>> getAllApproval(@RequestBody AllApprovalDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getAllApproval(dto));
    }

    @ApiOperation("获取已处理审批列表")
    @PostMapping("/getOverApproval")
    public ResultEntity<Page<EndingApprovalVO>> getOverApproval(@RequestBody EndingApprovalDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getOverApproval(dto));
    }

    @ApiOperation("获取审批流程详情")
    @PostMapping("/getApprovalDetail")
    public ResultEntity<ApprovalDetailVO> getApprovalDetail(Integer applyId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getApprovalDetail(applyId));
    }

    @ApiOperation("审批")
    @PostMapping("/approval")
    public ResultEntity<ResultEnum> approval(@RequestBody ApprovalDTO dto) {
        return ResultEntityBuild.build(processService.approval(dto));
    }
    @ApiOperation("提交批量审批任务")
    @PostMapping("/batchApproval")
    public ResultEntity<ResultEnum> batchApproval(@RequestBody BatchApprovalDTO dto) {
        return ResultEntityBuild.build(processService.batchApproval(dto));
    }
    @ApiOperation("执行批量审批任务")
    @PostMapping("/executeApproval")
    public ResultEntity<ResultEnum> executeApproval(@RequestBody List<ApprovalDTO> dataDto) {
        return ResultEntityBuild.build(processService.executeApproval(dataDto));
    }

    @ApiOperation("撤回审批")
    @GetMapping("/rollbackApproval")
    public ResultEntity<ResultEnum> rollbackApproval( Integer applyId) {
        return ResultEntityBuild.build(processService.rollbackApproval(applyId));
    }

    @ApiOperation("下载报告记录")
    @GetMapping("/downloadApprovalApply")
    @ControllerAOPConfig(printParams=false)
    public void downloadApprovalApply(Integer applyId, HttpServletResponse response) {
        processService.downloadApprovalApply(applyId, response);
    }
}
