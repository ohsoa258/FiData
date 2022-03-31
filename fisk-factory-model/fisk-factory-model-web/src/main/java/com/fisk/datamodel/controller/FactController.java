package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.service.IFact;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = { SwaggerConfig.FACT })
@RestController
@RequestMapping("/fact")
@Slf4j
public class FactController {
    @Resource
    IFact service;

    @ApiOperation("获取事实表列表")
    @PostMapping("/getFactList")
    public ResultEntity<Object> getFactList(@RequestBody QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactList(dto));
    }

    @ApiOperation("添加事实表")
    @PostMapping("/addFact")
    public ResultEntity<Object> addFact(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.addFact(dto));
    }

    @ApiOperation("根据id获取事实表详情")
    @GetMapping("/getFact/{id}")
    public ResultEntity<Object> getFact(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFact(id));
    }

    @ApiOperation("修改事实表")
    @PutMapping("/editFact")
    public ResultEntity<Object> editFact(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.updateFact(dto));
    }

    @ApiOperation("删除事实表")
    @DeleteMapping("/deleteFact/{id}")
    public ResultEntity<Object> deleteFact(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteFact(id));
    }

    @ApiOperation("获取事实表以及事实字段表数据")
    @GetMapping("/getFactDropList")
    public ResultEntity<Object> getFactDropList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactDropList());
    }

    @ApiOperation("获取事实表下拉列表")
    @GetMapping("/getFactScreenDropList")
    public ResultEntity<Object> getFactScreenDropList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactScreenDropList());
    }

    @ApiOperation("修改事实sql脚本")
    @PutMapping("/editFactSql")
    public ResultEntity<Object> editFactSql(@Validated @RequestBody DimensionSqlDTO dto) {
        return ResultEntityBuild.build(service.updateFactSql(dto));
    }

    @ApiOperation("修改事实发布状态")
    @PutMapping("/updateFactPublishStatus")
    public void updateFactPublishStatus(@RequestBody ModelPublishStatusDTO dto){
        service.updateFactPublishStatus(dto);
    }

}
