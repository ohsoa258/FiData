package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;


import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessPublishDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessPublishQueryDTO;
import com.fisk.datamodel.service.IBusinessProcess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.BUSINESS_PROCESS})
@RestController
@RequestMapping("/businessProcess")
@Slf4j
public class BusinessProcessController {
    @Resource
    IBusinessProcess service;

    @ApiOperation("获取业务过程列表")
    @PostMapping("/getBusinessProcessList")
    public ResultEntity<Object> getFactList(@RequestBody QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessProcessList(dto));
    }

    @ApiOperation("添加业务过程")
    @PostMapping("/addBusinessProcess")
    public ResultEntity<Object> addBusinessProcess(@Validated @RequestBody BusinessProcessDTO dto) {
        return ResultEntityBuild.build(service.addBusinessProcess(dto));
    }

    @ApiOperation("根据id获取业务过程详情")
    @GetMapping("/getBusinessProcess/{id}")
    public ResultEntity<Object> getBusinessProcess(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessProcessDetail(id));
    }

    @ApiOperation("修改业务过程")
    @PutMapping("/editBusinessProcess")
    public ResultEntity<Object> editBusinessProcess(@Validated @RequestBody BusinessProcessDTO dto) {
        return ResultEntityBuild.build(service.updateBusinessProcess(dto));
    }

    @ApiOperation("删除业务过程")
    @DeleteMapping("/deleteBusinessProcess")
    public ResultEntity<Object> deleteBusinessProcess(@Validated @RequestBody List<Integer> dto) {
        return ResultEntityBuild.build(service.deleteBusinessProcess(dto));
    }

    @ApiOperation("获取业务域下拉列表")
    @GetMapping("/getBusinessProcessDropList")
    public ResultEntity<Object> getBusinessProcessDropList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessProcessDropList());
    }

    @ApiOperation("根据业务过程id集合,发布相关事实表")
    @PostMapping("/publicBusinessProcess")
    public ResultEntity<Object> publicBusinessProcess(@Validated @RequestBody BusinessProcessPublishDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.businessProcessPublish(dto));
    }

    @ApiOperation("根据业务过程id,获取业务过程下相关事实")
    @GetMapping("/getBusinessProcessFact/{id}")
    public ResultEntity<Object> getBusinessProcessFact(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.businessProcessPush(id));
    }

    @ApiOperation("根据业务过程id获取业务域id")
    @GetMapping("/getBusinessId/{id}")
    public ResultEntity<Object> getBusinessId(@PathVariable("id")int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessId(id));
    }

    @ApiOperation("根据业务域id,获取业务过程列表")
    @GetMapping("/getBusinessProcessList/{businessAreaId}")
    public ResultEntity<Object> getBusinessProcessList(@PathVariable("businessAreaId") int businessAreaId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessProcessList(businessAreaId));
    }

    @ApiOperation("根据事实id集合,发布相关事实")
    @PostMapping("/publicFactFolder")
    public ResultEntity<Object> publicFactFolder(@Validated @RequestBody BusinessProcessPublishQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.batchPublishBusinessProcess(dto));
    }


}
