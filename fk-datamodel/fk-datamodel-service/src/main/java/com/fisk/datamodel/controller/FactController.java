package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
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
@Api(description = "业务过程")
@RestController
@RequestMapping("/fact")
@Slf4j
public class FactController {
    @Resource
    IFact service;

    @ApiOperation("获取业务过程列表")
    @PostMapping("/getFactList")
    public ResultEntity<Object> getFactList(@RequestBody QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactList(dto));
    }

    @ApiOperation("添加业务过程")
    @PostMapping("/addFact")
    public ResultEntity<Object> addFact(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.addFact(dto));
    }

    @ApiOperation("根据id获取业务过程详情")
    @GetMapping("/getFact/{id}")
    public ResultEntity<Object> getFact(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactDetail(id));
    }

    @ApiOperation("修改业务过程")
    @PutMapping("/editFact")
    public ResultEntity<Object> editFact(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.updateFact(dto));
    }

    @ApiOperation("删除业务过程")
    @DeleteMapping("/deleteFact/{id}")
    public ResultEntity<Object> deleteFact(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteFact(id));
    }


}
