package com.fisk.datamanagement.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.term.TermAssignedEntities;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.service.ICategory;
import com.fisk.datamanagement.service.IGlossary;
import com.fisk.datamanagement.service.ITerm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.GLOSSARY})
@RestController
@RequestMapping("/Glossary")
@Slf4j
public class GlossaryController {

    @Resource
    IGlossary service;
    @Resource
    ITerm iTerm;
    @Resource
    ICategory iCategory;

    @ApiOperation("获取术语库列表,包含术语库下术语、类别")
    @GetMapping("/getGlossaryList")
    public ResultEntity<Object> getGlossaryList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getGlossaryList());
    }

    @ApiOperation("添加术语库")
    @PostMapping("/addGlossary")
    public ResultEntity<Object> addGlossary(@Validated @RequestBody GlossaryDTO dto) {
        return ResultEntityBuild.build(service.addGlossary(dto));
    }

    @ApiOperation("删除术语库")
    @DeleteMapping("/deleteGlossary/{guid}")
    public ResultEntity<Object> deleteGlossary(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(service.deleteGlossary(guid));
    }

    @ApiOperation("修改术语库")
    @PutMapping("/updateGlossary")
    public ResultEntity<Object> updateGlossary(@Validated @RequestBody GlossaryDTO dto) {
        return ResultEntityBuild.build(service.updateGlossary(dto));
    }

    @ApiOperation("术语库下添加术语")
    @PostMapping("/addTerm")
    public ResultEntity<Object> addTerm(@Validated @RequestBody TermDTO dto) {
        return ResultEntityBuild.build(iTerm.addTerm(dto));
    }

    @ApiOperation("获取术语详情")
    @GetMapping("/getTerm/{guid}")
    public ResultEntity<Object> getTerm(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iTerm.getTerm(guid));
    }

    @ApiOperation("修改术语")
    @PutMapping("/updateTerm")
    public ResultEntity<Object> updateTerm(@Validated @RequestBody TermDTO dto) {
        return ResultEntityBuild.build(iTerm.updateTerm(dto));
    }

    @ApiOperation("删除术语")
    @DeleteMapping("/deleteTerm/{guid}")
    public ResultEntity<Object> deleteTerm(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(iTerm.deleteTerm(guid));
    }

    @ApiOperation("术语关联entity")
    @PostMapping("/termAssignedEntities")
    public ResultEntity<Object> termAssignedEntities(@Validated @RequestBody TermAssignedEntities dto) {
        return ResultEntityBuild.build(iTerm.termAssignedEntities(dto));
    }

    @ApiOperation("术语删除关联entity")
    @PutMapping("/termDeleteAssignedEntities")
    public ResultEntity<Object> termDeleteAssignedEntities(@Validated @RequestBody TermAssignedEntities dto) {
        return ResultEntityBuild.build(iTerm.termDeleteAssignedEntities(dto));
    }

    @ApiOperation("术语库下添加类别")
    @PostMapping("/addCategory")
    public ResultEntity<Object> addCategory(@Validated @RequestBody CategoryDTO dto) {
        return ResultEntityBuild.build(iCategory.addCategory(dto));
    }

    @ApiOperation("删除类别")
    @DeleteMapping("/deleteCategory/{guid}")
    public ResultEntity<Object> deleteCategory(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(iCategory.deleteCategory(guid));
    }

    @ApiOperation("获取类别详情")
    @GetMapping("/getCategory/{guid}")
    public ResultEntity<Object> getCategory(@PathVariable("guid") String guid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iCategory.getCategory(guid));
    }

    @ApiOperation("修改类别")
    @PutMapping("/updateCategory")
    public ResultEntity<Object> updateCategory(@Validated @RequestBody CategoryDTO dto) {
        return ResultEntityBuild.build(iCategory.updateCategory(dto));
    }


}
