package com.fisk.datamanagement.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetaDataDTO;
import com.fisk.datamanagement.service.IBusinessMetaData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.BUSINESS_META_DATA})
@RestController
@RequestMapping("/BusinessMetaData")
public class BusinessMetaDataController {

    @Resource
    IBusinessMetaData service;

    @ApiOperation("获取业务元数据列表")
    @GetMapping("/getBusinessMetaDataList")
    public ResultEntity<Object> getBusinessMetaDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessMetaDataList());
    }

    @ApiOperation("添加业务元数据")
    @PostMapping("/addBusinessMetaData")
    public ResultEntity<Object> addBusinessMetaData(@Validated @RequestBody BusinessMetaDataDTO dto) {
        return ResultEntityBuild.build(service.addBusinessMetaData(dto));
    }

    @ApiOperation("修改业务元数据属性")
    @PutMapping("/updateBusinessMetaData")
    public ResultEntity<Object> updateBusinessMetaData(@Validated @RequestBody BusinessMetaDataDTO dto) {
        return ResultEntityBuild.build(service.updateBusinessMetaData(dto));
    }

    @ApiOperation("删除业务元数据")
    @DeleteMapping("/deleteBusinessMetaData/{businessMetaDataName}")
    public ResultEntity<Object> deleteBusinessMetaData(@PathVariable("businessMetaDataName") String businessMetaDataName) {
        return ResultEntityBuild.build(service.deleteBusinessMetaData(businessMetaDataName));
    }

}
