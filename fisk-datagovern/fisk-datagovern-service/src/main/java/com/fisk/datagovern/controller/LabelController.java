package com.fisk.datagovern.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovern.config.SwaggerConfig;
import com.fisk.datagovern.dto.category.CategoryDTO;
import com.fisk.datagovern.dto.label.LabelDTO;
import com.fisk.datagovern.service.ILabel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.LABEL})
@RestController
@RequestMapping("/Label")
@Slf4j
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


}
