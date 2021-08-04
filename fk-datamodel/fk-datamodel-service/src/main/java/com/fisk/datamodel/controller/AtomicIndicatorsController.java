package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.service.IAtomicIndicators;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(description = "数仓建模--原子指标")
@RestController
@RequestMapping("/AtomicIndicators")
@Slf4j
public class AtomicIndicatorsController {
    @Resource
    IAtomicIndicators service;

    @ApiOperation("添加原子指标")
    @PostMapping("/addAtomicIndicators")
    public ResultEntity<Object> addAtomicIndicators(@Validated @RequestBody AtomicIndicatorsDTO dto) {
        return ResultEntityBuild.build(service.addAtomicIndicators(dto));
    }

    @ApiOperation("删除原子指标")
    @DeleteMapping("/deleteAtomicIndicators/{id}")
    public ResultEntity<Object> deleteAtomicIndicators(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteAtomicIndicators(id));
    }

    @ApiOperation("根据id获取原子指标详情")
    @GetMapping("/getAtomicIndicatorsDetails/{id}")
    public ResultEntity<Object> getAtomicIndicatorsDetails(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAtomicIndicatorDetails(id));
    }

    @ApiOperation("修改原子指标")
    @PutMapping("/editAtomicIndicators")
    public ResultEntity<Object> editAtomicIndicators(@Validated @RequestBody AtomicIndicatorsDTO dto) {
        return ResultEntityBuild.build(service.updateAtomicIndicatorDetails(dto));
    }

}
