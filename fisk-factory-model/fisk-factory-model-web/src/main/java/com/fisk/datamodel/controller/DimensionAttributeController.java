package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeUpdateDTO;
import com.fisk.datamodel.service.IDimensionAttribute;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
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
    public ResultEntity<Object> addAttribute(@Validated @RequestBody DimensionAttributeAddDTO dto) {
        return ResultEntityBuild.build(service.addOrUpdateDimensionAttribute(dto));
    }

    @ApiOperation("删除维度字段")
    @PostMapping("/deleteAttribute")
    public ResultEntity<Object> deleteAttribute(@RequestBody List<Integer> ids) {
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

    @GetMapping("/getDimensionAttributeDetail/{id}")
    @ApiOperation("根据维度字段id获取维度字段详情")
    public ResultEntity<Object> getDimensionAttributeDetail(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttribute(id));
    }

    @PostMapping("/getDimensionAttributeByIds")
    @ApiOperation("根据维度字段id集合获取维度字段详情")
    public ResultEntity<List<DimensionAttributeDTO>> getDimensionAttributeByIds(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttributeByIds(ids));
    }

    @GetMapping("/getDimensionAttributeData/{id}")
    @ApiOperation("根据维度id获取维度字段列表")
    public ResultEntity<Object> getDimensionAttributeData(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttributeData(id));
    }

    @GetMapping("/getDimensionAttributeDataList/{id}")
    @ApiOperation("根据维度id获取字段列表(宽表)")
    public ResultEntity<Object> getDimensionAttributeDataList(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttributeDataList(id));
    }

    @GetMapping("/selectDimensionAttributeList")
    @ApiOperation("根据维度id获取维度字段及其关联详情(nifi)")
    public ResultEntity<List<ModelPublishFieldDTO>> selectDimensionAttributeList(@RequestParam("dimensionId") int dimensionId) {
        return service.selectDimensionAttributeList(dimensionId);
    }

    @ApiOperation("新增单个维度字段")
    @PostMapping("/addDimensionAttribute")
    public ResultEntity<Object> addDimensionAttribute(@RequestBody DimensionAttributeDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addDimensionAttribute(dto));
    }

}
