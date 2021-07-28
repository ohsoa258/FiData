package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.DimensionDTO;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.service.IDimension;
import com.fisk.datamodel.service.IProjectDimensionAttribute;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(description = "数仓建模--维度")
@RestController
@RequestMapping("/dimension")
@Slf4j
public class DimensionController {
    @Resource
    IDimension service;
    @Resource
    IProjectDimensionAttribute iProjectDimensionAttribute;

    @GetMapping("/getDimension")
    @ApiOperation("获取维度列表")
    public ResultEntity<Object> getDimension(QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimension(dto));
    }

    /*@ApiOperation("获取数据域以及数据域下的维度表")
    @GetMapping("/getDataDimension")
    public ResultEntity<Object> getDataDimension() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionList());
    }*/

    @ApiOperation("添加维度获取数据域相关数据")
    @GetMapping("/getDimensionAssociation/{id}")
    public ResultEntity<Object> getDimensionAssociation(@PathVariable("id")int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRegionDetail(id));
    }

    @ApiOperation("添加维度")
    @PostMapping("/addDimension")
    public ResultEntity<Object> addDimension(@Validated @RequestBody DimensionDTO dto) {
        return ResultEntityBuild.build(service.addDimension(dto));
    }

    @ApiOperation("删除维度")
    @DeleteMapping("/deleteDimension/{id}")
    public ResultEntity<Object> deleteDimension(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteDimension(id));
    }

    @ApiOperation("根据id获取维度详情")
    @GetMapping("/getDimension/{id}")
    public ResultEntity<Object> getDimension(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimension(id));
    }

    @ApiOperation("修改维度")
    @PutMapping("/editDimension")
    public ResultEntity<Object> editDimension(@Validated @RequestBody DimensionDTO dto) {
        return ResultEntityBuild.build(service.updateDimension(dto));
    }

    @ApiOperation("获取数据接入表名以及字段")
    @GetMapping("/getDataAccessMeta")
    public ResultEntity<Object> getDataAccessMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iProjectDimensionAttribute.getProjectDimensionMeta());
    }

    @ApiOperation("获取维度表名以及字段")
    @GetMapping("/getDimensionMeta")
    public ResultEntity<Object> getDimensionMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iProjectDimensionAttribute.getProjectDimensionTable());
    }



}
