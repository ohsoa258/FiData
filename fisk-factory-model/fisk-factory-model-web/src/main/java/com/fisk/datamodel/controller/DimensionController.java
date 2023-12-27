package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dimension.*;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.service.IDimension;
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
@Api(tags = {SwaggerConfig.DIMENSION})
@RestController
@RequestMapping("/dimension")
@Slf4j
public class DimensionController {
    @Resource
    IDimension service;

    @ApiOperation("获取关联维度列表")
    @PostMapping("/getAssociateDimensionList")
    public ResultEntity<Object> getAssociateDimensionList(@RequestBody DimensionQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionNameList(dto));
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

    @ApiOperation("修改维度sql脚本")
    @PutMapping("/editDimensionSql")
    public ResultEntity<Object> editDimensionSql(@Validated @RequestBody DimensionSqlDTO dto) {
        return ResultEntityBuild.build(service.updateDimensionSql(dto));
    }

    @ApiOperation("根据业务域id获取设置时间维度")
    @GetMapping("/getDimensionDateAttribute/{id}")
    public ResultEntity<Object> getDimensionDateAttribute(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionDateAttribute(id));
    }

    @ApiOperation("修改业务域下时间维度表以及字段")
    @PutMapping("/updateDimensionDateAttribute")
    public ResultEntity<Object> updateDimensionDateAttribute(@Validated @RequestBody DimensionDateAttributeDTO dto) {
        return ResultEntityBuild.build(service.updateDimensionDateAttribute(dto));
    }

    @ApiOperation("修改维度发布状态")
    @PutMapping("/updateDimensionPublishStatus")
    public void updateDimensionPublishStatus(@RequestBody ModelPublishStatusDTO dto) {
        service.updateDimensionPublishStatus(dto);
    }

    @ApiOperation("获取发布成功所有维度表")
    @GetMapping("/getPublishSuccessDimTable/{businessId}")
    public ResultEntity<Object> getPublishSuccessDimTable(@PathVariable("businessId") int businessId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getPublishSuccessDimTable(businessId));
    }

    /**
     * 根据表名获取事实或维度表
     *
     * @param tblName
     * @return
     */
    @ApiOperation("根据表名获取事实或维度表")
    @GetMapping("/getFactOrDimTable")
    public ResultEntity<Long> getFactOrDimTable(@RequestParam("tblName") String tblName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactOrDimTable(tblName));
    }

    /**
     * 获取维度tree
     *
     * @return
     */
    @ApiOperation("获取维度tree")
    @GetMapping("/getDimensionTree")
    public List<DimensionTreeDTO> getDimensionTree() {
        return service.getDimensionTree();
    }


}
