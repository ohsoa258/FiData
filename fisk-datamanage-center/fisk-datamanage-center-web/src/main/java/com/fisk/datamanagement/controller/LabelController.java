package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.label.LabelDTO;
import com.fisk.datamanagement.dto.label.LabelQueryDTO;
import com.fisk.datamanagement.service.ILabel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.LABEL})
@RestController
@RequestMapping("/Label")
public class LabelController {
    @Resource
    ILabel service;

    @ApiOperation("添加标签")
    @PostMapping("/addLabel")
    public ResultEntity<Object> addLabel(@Validated @RequestBody LabelDTO dto) {
        return ResultEntityBuild.build(service.addLabel(dto));
    }

    @ApiOperation("删除标签")
    @DeleteMapping("/delLabel/{id}")
    public ResultEntity<Object> delLabel(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.delLabel(id));
    }

    @ApiOperation("根据id获取标签")
    @GetMapping("/getLabelDetail/{id}")
    public ResultEntity<Object> getLabelDetail(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getLabelDetail(id));
    }

    @ApiOperation("修改标签")
    @PutMapping("/updateLabel")
    public ResultEntity<Object> updateLabel(@Validated @RequestBody LabelDTO dto) {
        return ResultEntityBuild.build(service.updateLabel(dto));
    }

    @ApiOperation("根据选中类目id获取标签列表")
    @PostMapping("/getLabelPageList")
    public ResultEntity<Object> getLabelPageList(@RequestBody LabelQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getLabelPageList(dto));
    }

    @ApiOperation("atlas获取标签列表")
    @GetMapping("/atlasGetLabelList")
    public ResultEntity<Object> atlasGetLabelList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.atlasGetLabel());
    }


}
