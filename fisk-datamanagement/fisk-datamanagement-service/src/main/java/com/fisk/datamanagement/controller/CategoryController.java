package com.fisk.datamanagement.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.service.ICategory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.CATEGORY})
@RestController
@RequestMapping("/Category")
@Slf4j
public class CategoryController {
    @Resource
    ICategory service;

    @ApiOperation("添加标签类目")
    @PostMapping("/addCategory")
    public ResultEntity<Object> addCategory(@Validated @RequestBody CategoryDTO dto) {
        return ResultEntityBuild.build(service.addCategory(dto));
    }

    @ApiOperation("删除标签类目")
    @DeleteMapping("/delCategory/{id}")
    public ResultEntity<Object> delCategory(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.delCategory(id));
    }

    @ApiOperation("根据id获取标签类目详情")
    @GetMapping("/getCategoryDetail/{id}")
    public ResultEntity<Object> getCategoryDetail(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCategoryDetail(id));
    }

    @ApiOperation("修改类目详情")
    @PutMapping("/updateCategory")
    public ResultEntity<Object> updateCategory(@Validated @RequestBody CategoryDTO dto) {
        return ResultEntityBuild.build(service.updateCategory(dto));
    }

    @ApiOperation("获取类目列表")
    @GetMapping("/getCategoryList")
    public ResultEntity<Object> getCategoryList(String queryName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCategoryList(queryName));
    }



}
