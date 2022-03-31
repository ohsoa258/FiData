package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsListDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsQueryDTO;
import com.fisk.datamodel.service.IDerivedIndicators;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DERIVED_INDICATOR})
@RestController
@RequestMapping("/DerivedIndicators")
@Slf4j
public class DerivedIndicatorsController {
    @Resource
    IDerivedIndicators service;

    @PostMapping("/getDerivedIndicatorsList")
    @ApiOperation(value = "获取派生指标数据列表")
    public ResultEntity<Page<DerivedIndicatorsListDTO>> getDerivedIndicatorsList(@RequestBody DerivedIndicatorsQueryDTO query){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getDerivedIndicatorsList(query));
    }

    @ApiOperation("删除派生指标")
    @DeleteMapping("/deleteDerivedIndicators/{id}")
    public ResultEntity<Object> deleteDerivedIndicators(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteDerivedIndicators(id));
    }

    @ApiOperation("添加派生指标")
    @PostMapping("/addDerivedIndicators")
    public ResultEntity<Object> addDerivedIndicators(@Validated @RequestBody DerivedIndicatorsDTO dto) {
        return ResultEntityBuild.build(service.addDerivedIndicators(dto));
    }

    @ApiOperation("根据id获取派生指标详情")
    @GetMapping("/getDerivedIndicatorsDetails/{id}")
    public ResultEntity<Object> getDerivedIndicatorsDetails(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDerivedIndicators(id));
    }

    @ApiOperation("修改派生指标")
    @PutMapping("/editDerivedIndicators")
    public ResultEntity<Object> editDerivedIndicators(@Validated @RequestBody DerivedIndicatorsDTO dto) {
        return ResultEntityBuild.build(service.updateDerivedIndicators(dto));
    }

    /*@ApiOperation("根据id获取派生指标聚合粒度")
    @GetMapping("/getDerivedIndicatorsParticle/{id}")
    public ResultEntity<Object> getDerivedIndicatorsParticle(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDerivedIndicatorsParticle(id));
    }*/

    @ApiOperation("根据业务域id获取指标列表")
    @GetMapping("/getIndicators/{id}")
    public ResultEntity<Object> getIndicators(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getIndicatorsList(id));
    }

}
