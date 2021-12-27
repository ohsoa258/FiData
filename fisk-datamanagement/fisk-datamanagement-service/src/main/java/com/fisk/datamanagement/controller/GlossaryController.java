package com.fisk.datamanagement.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.service.IGlossary;
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

}
