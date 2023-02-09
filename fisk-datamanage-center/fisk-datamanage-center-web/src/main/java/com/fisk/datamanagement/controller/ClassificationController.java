package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.classification.ClassificationAddEntityDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDelAssociatedEntityDTO;
import com.fisk.datamanagement.service.IClassification;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.CLASSIFICATION})
@RestController
@RequestMapping("/Classification")
@Slf4j
public class ClassificationController {
    @Resource
    IClassification service;

    @ApiOperation("获取业务分类列表")
    @GetMapping("/getClassificationList")
    public ResultEntity<Object> getClassificationList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getClassificationList());
    }

    @ApiOperation("获取业务类型树形列表-已重构")
    @GetMapping("/getClassificationTree")
    public ResultEntity<Object> getClassificationTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getClassificationTree());
    }

    @ApiOperation("修改业务类型以及业务属性")
    @PutMapping("/updateClassification")
    public ResultEntity<Object> updateClassification(@Validated @RequestBody ClassificationDefsDTO dto) {
        return ResultEntityBuild.build(service.updateClassification(dto));
    }

    @ApiOperation("根据业务分类名称删除-已重构")
    @DeleteMapping("/deleteClassification/{classificationName}")
    public ResultEntity<Object> deleteClassification(@PathVariable("classificationName") String classificationName) {
        return ResultEntityBuild.build(service.deleteClassification(classificationName));
    }

    @ApiOperation("添加业务分类以及属性-已重构")
    @PostMapping("/addClassification")
    public ResultEntity<Object> addClassification(@Validated @RequestBody ClassificationDefsDTO dto) {
        return ResultEntityBuild.build(service.addClassification(dto, null));
    }

    @ApiOperation("业务分类添加关联entity")
    @PostMapping("/classificationAddAssociatedEntity")
    public ResultEntity<Object> classificationAddAssociatedEntity(@Validated @RequestBody ClassificationAddEntityDTO dto) {
        return ResultEntityBuild.build(service.classificationAddAssociatedEntity(dto));
    }

    @ApiOperation("业务分类删除关联entity")
    @DeleteMapping("/classificationDelAssociatedEntity")
    public ResultEntity<Object> classificationDelAssociatedEntity(@Validated @RequestBody ClassificationDelAssociatedEntityDTO dto) {
        return ResultEntityBuild.build(service.classificationDelAssociatedEntity(dto));
    }

    @ApiOperation("同步业务分类")
    @PostMapping("/synchronousClassification")
    public ResultEntity<Object> synchronousClassification() {
        return ResultEntityBuild.build(service.synchronousClassification());
    }

    @ApiOperation("数据接入应用同步到业务分类")
    @PostMapping("/appSynchronousClassification")
    public ResultEntity<Object> appSynchronousClassification(@Validated @RequestBody ClassificationInfoDTO dto) {
        return ResultEntityBuild.build(service.appSynchronousClassification(dto));
    }

    @ApiOperation("获取业务类型树形列表")
    @GetMapping("/delClassificationEntity")
    public ResultEntity<Object> delClassificationEntity(String classification) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.delClassificationEntity(classification));
    }

}
