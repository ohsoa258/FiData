package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessDTO;
import com.fisk.datamodel.service.IBusinessProcess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    @DeleteMapping("/deleteBusinessProcess/{id}")
    public ResultEntity<Object> deleteBusinessProcess(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteBusinessProcess(id));
    }

    @ApiOperation("获取业务域下拉列表")
    @GetMapping("/getBusinessProcessDropList")
    public ResultEntity<Object> getBusinessProcessDropList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessProcessDropList());
    }

    @ApiOperation("根据业务过程id发布")
    @GetMapping("/publicBusinessProcess/{id}")
    public ResultEntity<Object> publicBusinessProcess(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.businessProcessPublish(id));
    }


}
