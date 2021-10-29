package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.service.IDimension;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags ={SwaggerConfig.DIMENSION})
@RestController
@RequestMapping("/dimension")
@Slf4j
public class DimensionController {
    @Resource
    IDimension service;

    /*@ApiOperation("获取维度列表")
    @PostMapping("/getDimensionList")
    public ResultEntity<Object> getDimensionList(@RequestBody QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionList(dto));
    }*/

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

    /*@ApiOperation("根据维度id发布")
    @GetMapping("/publicDimension/{id}")
    public ResultEntity<Object> publicDimension(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.dimensionPublish(id));
    }

    @ApiOperation("修改维度发布状态")
    @PutMapping("/editPublishStatus")
    public void editPublishStatus(@RequestParam("id")int id,@RequestParam("isSuccess")int isSuccess) {
        service.updatePublishStatus(id,isSuccess);
    }*/

}
