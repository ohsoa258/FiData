package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.DimensionAttributeDTO;
import com.fisk.datamodel.dto.DimensionAttributeUpdateDTO;
import com.fisk.datamodel.dto.DimensionDTO;
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
@Api(description = "数仓建模--维度字段")
@RestController
@RequestMapping("/attribute")
@Slf4j
public class DimensionAttributeController {
    @Resource
    IDimensionAttribute service;

    @ApiOperation("获取数据接入表名以及字段")
    @GetMapping("/getDataAccessMeta")
    public ResultEntity<Object> getDataAccessMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getProjectDimensionMeta());
    }

    @ApiOperation("获取关联维度表名以及字段")
    @GetMapping("/getDimensionMeta")
    public ResultEntity<Object> getDimensionMeta() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getProjectDimensionTable());
    }

    @ApiOperation("添加维度字段")
    @PostMapping("/addAttribute")
    public ResultEntity<Object> addAttribute(@Validated @RequestBody DimensionAttributeAddDTO dto)
    {
        return ResultEntityBuild.build(service.addDimensionAttribute(dto.dimensionId,dto.list));
    }

    @ApiOperation("删除维度字段")
    @PostMapping("/deleteAttribute")
    public ResultEntity<Object> deleteAttribute(@Validated @RequestBody List<Integer> ids)
    {
        return ResultEntityBuild.build(service.deleteDimensionAttribute(ids));
    }

    @ApiOperation("获取维度字段表列表")
    @GetMapping("/getDimensionAttributeList")
    public ResultEntity<Object> getDimensionAttributeList(int dimensionId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionAttributeList(dimensionId));
    }

    @ApiOperation("修改维度字段")
    @PutMapping("/editDimensionAttribute")
    public ResultEntity<Object> editDimensionAttribute(@Validated @RequestBody DimensionAttributeUpdateDTO dto) {
        return ResultEntityBuild.build(service.updateDimensionAttribute(dto));
    }

}
