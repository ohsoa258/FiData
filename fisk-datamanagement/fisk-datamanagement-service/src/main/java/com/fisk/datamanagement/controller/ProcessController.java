package com.fisk.datamanagement.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.datamanagement.dto.label.LabelDTO;
import com.fisk.datamanagement.dto.process.AddProcessDTO;
import com.fisk.datamanagement.service.IProcess;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
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

}
