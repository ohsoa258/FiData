package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.process.AddProcessDTO;
import com.fisk.datamanagement.dto.process.ProcessDTO;
import com.fisk.datamanagement.service.IProcess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.PROCESS})
@RestController
@RequestMapping("/Process")
public class ProcessController {

    @Resource
    IProcess service;

    @ApiOperation("添加process")
    @PostMapping("/addProcess")
    public ResultEntity<Object> addProcess(@Validated @RequestBody AddProcessDTO dto) {
        return ResultEntityBuild.build(service.addProcess(dto));
    }

    @ApiOperation("获取process详情.已重构")
    @GetMapping("/getProcess/{guid}")
    public ResultEntity<Object> getProcess(@PathVariable String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getProcess(guid));
    }

    @ApiOperation("更新process")
    @PostMapping("/updateProcess")
    public ResultEntity<Object> updateProcess(@Validated @RequestBody ProcessDTO dto) {
        return ResultEntityBuild.build(service.updateProcess(dto));
    }

    @ApiOperation("删除process")
    @DeleteMapping("/deleteProcess")
    public ResultEntity<Object> deleteProcess(String guid) {
        return ResultEntityBuild.build(service.deleteProcess(guid));
    }

}
