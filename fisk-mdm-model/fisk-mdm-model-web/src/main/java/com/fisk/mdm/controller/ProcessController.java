package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.process.ApprovalDTO;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.dto.process.ProcessInfoDTO;
import com.fisk.mdm.service.ProcessService;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import com.fisk.mdm.vo.process.ProcessApplyVO;
import com.fisk.mdm.vo.process.ProcessInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    @ResponseBody
    public ResultEntity<ProcessInfoVO> getProcessEntity(Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getProcess(entityId));
    }

    @ApiOperation("保存流程实体")
    @PostMapping("/saveProcessEntity")
    @ResponseBody
    public ResultEntity<ResultEnum> saveProcessEntity(@RequestBody ProcessInfoDTO dto) {
        return ResultEntityBuild.build(processService.saveProcess(dto));
    }
    @ApiOperation("校验是否需要走流程")
    @GetMapping("/verifyProcessApply")
    @ResponseBody
    public ResultEntity<Boolean> verifyProcessApply(Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.verifyProcessApply(entityId));
    }

    @ApiOperation("获取我的申请")
    @GetMapping("/getMyProcessApply")
    @ResponseBody
    public ResultEntity<List<ProcessApplyVO>> getMyProcessApply() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getMyProcessApply());
    }

    @ApiOperation("获取待处理审批列表")
    @PostMapping("/getPendingApproval")
    @ResponseBody
    public ResultEntity<Page<PendingApprovalVO>> getPendingApproval(@RequestBody PendingApprovalDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getPendingApproval(dto));
    }

    @ApiOperation("获取已处理审批列表")
    @PostMapping("/getOverApproval")
    @ResponseBody
    public ResultEntity<Page<PendingApprovalVO>> getOverApproval(@RequestBody PendingApprovalDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.getOverApproval(dto));
    }

    @ApiOperation("审批")
    @PostMapping("/approval")
    @ResponseBody
    public ResultEntity<ResultEnum> approval(@RequestBody ApprovalDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,processService.approval(dto));
    }
}
