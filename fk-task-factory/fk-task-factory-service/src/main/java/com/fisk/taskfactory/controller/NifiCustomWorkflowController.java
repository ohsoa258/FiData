package com.fisk.taskfactory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppRegistrationQueryDTO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.taskfactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.taskfactory.dto.customworkflow.NifiCustomWorkflowQueryDTO;
import com.fisk.taskfactory.service.INifiCustomWorkflow;
import com.fisk.taskfactory.vo.customworkflow.NifiCustomWorkflowVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    public ResultEntity<NifiCustomWorkflowDTO> getData(@PathVariable("id") long id) {

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
    public ResultEntity<Object> getBusinessColumn(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getColumn());
    }

    @PostMapping("/pageFilter")
    @ApiOperation(value = "筛选器")
    public ResultEntity<Page<NifiCustomWorkflowVO>> listData(@RequestBody NifiCustomWorkflowQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }
}
