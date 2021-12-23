package com.fisk.datamanagement.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.service.IEntity;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/Entity")
@Slf4j
public class MetaDataEntityController {

    @Resource
    IEntity service;

    @ApiOperation("获取atlas元数据对象树形列表")
    @GetMapping("/getEntityList")
    public ResultEntity<Object> getEntityList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getEntityTreeList());
    }

    @ApiOperation("添加元数据对象：实例、数据库、表、字段")
    @PostMapping("/addEntity")
    public ResultEntity<Object> addEntity(@Validated @RequestBody EntityDTO dto) {
        return ResultEntityBuild.build(service.addEntity(dto));
    }

    @ApiOperation("根据guid删除元数据对象")
    @DeleteMapping("/deleteEntity/{guid}")
    public ResultEntity<Object> deleteEntity(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(service.deleteEntity(guid));
    }

    @ApiOperation("根据guid获取entity详情")
    @GetMapping("/getEntityDetail/{guid}")
    public ResultEntity<Object> getCategoryDetail(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getEntity(guid));
    }

}
