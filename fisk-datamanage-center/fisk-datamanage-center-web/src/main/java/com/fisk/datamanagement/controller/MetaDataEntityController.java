package com.fisk.datamanagement.controller;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.entity.EntityAssociatedLabelDTO;
import com.fisk.datamanagement.dto.entity.EntityAssociatedMetaDataDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityFilterDTO;
import com.fisk.datamanagement.service.IEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.META_DATA_ENTITY})
@RestController
@RequestMapping("/Entity")
public class MetaDataEntityController {

    @Resource
    IEntity service;

    @ApiOperation("获取atlas元数据对象树形列表")
    @GetMapping("/getEntityList")
    public ResultEntity<Object> getEntityList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getEntityTreeList());
    }

    @ApiOperation("添加元数据对象：实例、数据库、表、字段、血缘")
    @PostMapping("/addEntity")
    public ResultEntity<Object> addEntity(@Validated @RequestBody EntityDTO dto) {
        return ResultEntityBuild.build(service.addEntity(dto));
    }

    @ApiOperation("根据guid删除元数据对象/血缘")
    @DeleteMapping("/deleteEntity/{guid}")
    public ResultEntity<Object> deleteEntity(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(service.deleteEntity(guid));
    }

    @ApiOperation("根据guid获取entity详情")
    @GetMapping("/getEntityDetail/{guid}")
    public ResultEntity<Object> getEntityDetail(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getEntity(guid));
    }

    @ApiOperation("更新元数据对象：实例、数据库、表、字段")
    @PostMapping("/updateEntity")
    public ResultEntity<Object> updateEntity(@Validated @RequestBody JSONObject dto) {
        return ResultEntityBuild.build(service.updateEntity(dto));
    }

    @ApiOperation("根据不同条件,筛选元数据对象列表")
    @PostMapping("/searchBasicEntity")
    public ResultEntity<Object> searchBasicEntity(@Validated @RequestBody EntityFilterDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.searchBasicEntity(dto));
    }

    @ApiOperation("根据guid获取实体审计列表")
    @GetMapping("/getAuditsList/{guid}")
    public ResultEntity<Object> getAuditsList(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAuditsList(guid));
    }

    @ApiOperation("实体添加标签")
    @PostMapping("/entityAssociatedLabel")
    public ResultEntity<Object> entityAssociatedLabel(@Validated @RequestBody EntityAssociatedLabelDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.entityAssociatedLabel(dto));
    }

    @ApiOperation("实体添加业务元数据")
    @PostMapping("/entityAssociatedMetaData")
    public ResultEntity<Object> entityAssociatedMetaData(@Validated @RequestBody EntityAssociatedMetaDataDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.entityAssociatedMetaData(dto));
    }

    @ApiOperation("根据guid获取血缘关系")
    @GetMapping("/getMetaDataKinship/{guid}")
    public ResultEntity<Object> getMetaDataKinship(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetaDataKinship(guid));
    }

    @ApiOperation("根据db实体id,获取实例详情")
    @GetMapping("/getInstanceDetail/{guid}")
    public ResultEntity<Object> getInstanceDetail(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getInstanceDetail(guid));
    }

}
