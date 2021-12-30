package com.fisk.datamanagement.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDTO;
import com.fisk.datamanagement.service.ILabelCategory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.LABEL_CATEGORY})
@RestController
@RequestMapping("/LabelCategory")
@Slf4j
public class LabelCategoryController {
    @Resource
    ILabelCategory service;

    @ApiOperation("添加标签类目")
    @PostMapping("/addLabelCategory")
    public ResultEntity<Object> addLabelCategory(@Validated @RequestBody LabelCategoryDTO dto) {
        return ResultEntityBuild.build(service.addLabelCategory(dto));
    }

    @ApiOperation("删除标签类目")
    @DeleteMapping("/delLabelCategory/{id}")
    public ResultEntity<Object> delLabelCategory(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.delLabelCategory(id));
    }

    @ApiOperation("根据id获取标签类目详情")
    @GetMapping("/getLabelCategoryDetail/{id}")
    public ResultEntity<Object> getLabelCategoryDetail(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCategoryDetail(id));
    }

    @ApiOperation("修改类目详情")
    @PutMapping("/updateLabelCategory")
    public ResultEntity<Object> updateLabelCategory(@Validated @RequestBody LabelCategoryDTO dto) {
        return ResultEntityBuild.build(service.updateLabelCategory(dto));
    }

    @ApiOperation("获取类目列表")
    @GetMapping("/getLabelCategoryList")
    public ResultEntity<Object> getLabelCategoryList(String queryName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getLabelCategoryList(queryName));
    }



}
