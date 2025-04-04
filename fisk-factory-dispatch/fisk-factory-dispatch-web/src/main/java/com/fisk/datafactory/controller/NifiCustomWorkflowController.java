package com.fisk.datafactory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.config.SwaggerConfig;
import com.fisk.datafactory.dto.customworkflow.*;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.NIFI_CUSTOM_WORKFLOW})
@RestController
@RequestMapping("/nifiCustomWorkflow")
@Slf4j
public class NifiCustomWorkflowController {

    @Resource
    INifiCustomWorkflow service;

    @ApiOperation("添加管道")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody NifiCustomWorkflowDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据")
    public ResultEntity<NifiCustomWorkflowDetailVO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改管道")
    public ResultEntity<Object> editData(@RequestBody NifiCustomWorkflowDTO dto) {

        return ResultEntityBuild.build(service.editData(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除管道")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取管道过滤器表字段")
    public ResultEntity<Object> getBusinessColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }

    @PostMapping("/pageFilter")
    @ApiOperation(value = "筛选器")
    public ResultEntity<Page<NifiCustomWorkflowVO>> listData(@RequestBody NifiCustomWorkflowQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }

    @ApiOperation("修改管道发布状态")
    @PutMapping("/updatePublishStatus")
    public void updatePublishStatus(@RequestBody NifiCustomWorkflowDTO dto) {
        service.updatePublishStatus(dto);
    }

    @GetMapping("/getTableListById/{id}")
    @ApiOperation(value = "根据管道id查询组件绑定的表集合")
    public ResultEntity<Object> getTableListById(@PathVariable("id") Long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableListById(id));
    }

    @PutMapping("/updateWorkStatus")
    @ApiOperation("暂停/恢复管道运行")
    public ResultEntity<Object> updateWorkStatus(@Validated @RequestBody NifiCustomWorkflowUpdateDTO dto){
        return service.updateWorkStatus(dto.getNifiCustomWorkflowId(), dto.getIfFire());
    }

    @GetMapping("/getNifiCustomWorkFlowDrop")
    @ApiOperation(value = "获取所有管道")
    public ResultEntity<Object> getNifiCustomWorkFlowDrop() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getNifiCustomWorkFlowDrop());
    }

    @GetMapping("/getNifiCustomWorkFlowPartInfo")
    @ApiOperation(value = "依据pipelTraceId获取管道部分字段信息")
    public ResultEntity<Object> getNifiCustomWorkFlowPartInfo(@RequestParam("pipelTraceId") String pipelTraceId){
        return service.getNifiCustomWorkFlowPartInfo(pipelTraceId);
    }

    @GetMapping("/getNifiCustomWorkFlow/{workflowId}")
    @ApiOperation(value = "根据workflowId获取管道信息")
    public ResultEntity<NifiCustomWorkflowDTO> getNifiCustomWorkFlow(@PathVariable("workflowId")String workflowId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getNifiCustomWorkFlow(workflowId));
    }

    @PostMapping("/getWorkFlowNameByTableId")
    @ApiOperation(value = "根据表组件表id获取管道名称及id")
    public ResultEntity<List<WorkflowDTO>> getWorkFlowNameByTableId(@RequestBody WorkFlowQueryDTO workFlowQueryDTO) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getWorkFlowNameByTableId(workFlowQueryDTO));
    }
}
