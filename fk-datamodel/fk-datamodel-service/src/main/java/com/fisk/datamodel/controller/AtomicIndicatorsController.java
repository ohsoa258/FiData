package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsQueryDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsResultDTO;
import com.fisk.datamodel.service.IAtomicIndicators;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.ATOMIC_INDICATOR})
@RestController
@RequestMapping("/AtomicIndicators")
@Slf4j
public class AtomicIndicatorsController {
    @Resource
    IAtomicIndicators service;

    @ApiOperation("添加原子指标")
    @PostMapping("/addAtomicIndicators")
    public ResultEntity<Object> addAtomicIndicators(@Validated @RequestBody List<AtomicIndicatorsDTO> dto) {
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

    @PostMapping("/getAtomicIndicatorsList")
    @ApiOperation(value = "获取原子指标数据列表")
    public ResultEntity<Page<AtomicIndicatorsResultDTO>> getAtomicIndicatorsList(@RequestBody AtomicIndicatorsQueryDTO query){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getAtomicIndicatorList(query));
    }

    @ApiOperation("根据原子指标下拉列表")
    @GetMapping("/getAtomicIndicatorsDropList")
    public ResultEntity<Object> getAtomicIndicatorsDropList(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.atomicIndicatorDropList(id));
    }

}
