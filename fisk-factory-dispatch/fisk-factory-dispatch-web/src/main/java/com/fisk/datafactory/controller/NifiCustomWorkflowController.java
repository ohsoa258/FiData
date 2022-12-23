package com.fisk.datafactory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowQueryDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowUpdateDTO;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author Lock
 */
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
    public void updatePublishStatus(@RequestBody NifiCustomWorkflowDTO dto){
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
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.updateWorkStatus(dto.getNifiCustomWorkflowId(), dto.getIfFire()));
    }
}
