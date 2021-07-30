package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
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
@Api(description = "事实表")
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
    @PostMapping("/addBusinessProcess")
    public ResultEntity<Object> addBusinessProcess(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.addFact(dto));
    }

    @ApiOperation("根据id获取事实表详情")
    @GetMapping("/getBusinessProcess/{id}")
    public ResultEntity<Object> getBusinessProcess(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFact(id));
    }

    @ApiOperation("修改事实表")
    @PutMapping("/editBusinessProcess")
    public ResultEntity<Object> editBusinessProcess(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.updateFact(dto));
    }

    @ApiOperation("删除事实表")
    @DeleteMapping("/deleteBusinessProcess/{id}")
    public ResultEntity<Object> deleteBusinessProcess(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteFact(id));
    }

}
