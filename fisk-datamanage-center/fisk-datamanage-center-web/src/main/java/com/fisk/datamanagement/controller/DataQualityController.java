package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.dataquality.DataQualityDTO;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;
import com.fisk.datamanagement.service.IDataQuality;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_QUALITY})
@RestController
@RequestMapping("/DataQuality")
public class DataQualityController {

    @Resource
    IDataQuality service;

    @ApiOperation("是否存在atlas")
    @PostMapping("/existAtlas")
    public ResultEntity<Object> existAtlas(@Validated @RequestBody DataQualityDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.existAtlas(dto));
    }

    @ApiOperation("是否存在上下血缘")
    @PostMapping("/existUpperLowerBlood")
    public ResultEntity<Object> existUpperLowerBlood(@Validated @RequestBody UpperLowerBloodParameterDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.existUpperLowerBlood(dto));
    }

}
