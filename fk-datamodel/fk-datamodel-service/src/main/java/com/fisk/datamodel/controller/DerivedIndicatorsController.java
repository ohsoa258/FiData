package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsListDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsQueryDTO;
import com.fisk.datamodel.service.IDerivedIndicators;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

}
