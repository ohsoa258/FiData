package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeUpdateDTO;
import com.fisk.datamodel.service.IDimensionAttribute;
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
@Api(tags = {SwaggerConfig.DIMENSION_ATTRIBUTE})
@RestController
@RequestMapping("/attribute")
@Slf4j
public class DimensionAttributeController {
    @Resource
    IDimensionAttribute service;

    @ApiOperation("添加维度字段")
    @PostMapping("/addAttribute")
    public ResultEntity<Object> addAttribute(@Validated @RequestBody DimensionAttributeAddDTO dto)
    {
        return ResultEntityBuild.build(service.addOrUpdateDimensionAttribute(dto.dimensionId,dto.isPublish,dto.list));
    }

    @ApiOperation("删除维度字段")
    @PostMapping("/deleteAttribute")
    public ResultEntity<Object> deleteAttribute(@RequestBody List<Integer> ids)
    {
        return ResultEntityBuild.build(service.deleteDimensionAttribute(ids));
    }

    @ApiOperation("获取维度字段表列表")
    @GetMapping("/getDimensionAttributeList/{id}")
    public ResultEntity<Object> getDimensionAttributeList(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttributeList(id));
    }

    @ApiOperation("修改维度字段")
    @PutMapping("/editDimensionAttribute")
    public ResultEntity<Object> editDimensionAttribute(@Validated @RequestBody DimensionAttributeUpdateDTO dto) {
        return ResultEntityBuild.build(service.updateDimensionAttribute(dto));
    }

    @GetMapping("/getDimensionEntity")
    @ApiOperation("获取维度表元数据(用于DW创建表)")
    public ResultEntity<Object> getDimensionEntity(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionMetaData(id));
    }

    @GetMapping("/getDimensionAttributeDetail/{id}")
    @ApiOperation("根据维度字段id获取维度字段详情")
    public ResultEntity<Object> getDimensionAttributeDetail(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttribute(id));
    }

    @GetMapping("/getDimensionAttributeData/{id}")
    @ApiOperation("根据维度id获取维度字段列表")
    public ResultEntity<Object> getDimensionAttributeData(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttributeData(id));
    }

    /*@GetMapping("/getDimensionListEntity")
    @ApiOperation("获取维度表元数据(用于Doris创建表)")
    public ResultEntity<Object> getDimensionListEntity(@RequestParam("businessAreaId") int businessAreaId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionMetaDataList(businessAreaId));
    }*/

    @ApiOperation("根据维度id获取维度字段所有来源id")
    @GetMapping("/getDimensionAttributeSourceId/{id}")
    public ResultEntity<Object> getDimensionAttributeSourceId(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttributeSourceId(id));
    }

}
