package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.ProjectDimensionDTO;
import com.fisk.datamodel.service.IProjectDimension;
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
public class ProjectDimensionController {
    @Resource
    IProjectDimension service;

    @ApiOperation("获取数据域以及数据域下的维度表")
    @GetMapping("/getDataDimension")
    public ResultEntity<Object> getDataDimension() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionList());
    }

    @ApiOperation("添加维度获取数据域相关数据")
    @GetMapping("/getDimensionAssociation/{id}")
    public ResultEntity<Object> getDimensionAssociation(@PathVariable("id")int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getRegionDetail(id));
    }

    @ApiOperation("添加维度")
    @PostMapping("/addDimension")
    public ResultEntity<Object> addDimension(@Validated @RequestBody ProjectDimensionDTO dto) {
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
    public ResultEntity<Object> editDimension(@Validated @RequestBody ProjectDimensionDTO dto) {
        return ResultEntityBuild.build(service.updateDimension(dto));
    }

    @ApiOperation("根据数据域id获取项目列表")
    @GetMapping("/getProjectInfo/{id}")
    public ResultEntity<Object> getProjectInfo(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getProjectDropList(id));
    }
}
