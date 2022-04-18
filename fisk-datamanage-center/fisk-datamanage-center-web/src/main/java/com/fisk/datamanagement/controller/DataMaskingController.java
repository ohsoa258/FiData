package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import com.fisk.datamanagement.dto.datamasking.SourceTableDataDTO;
import com.fisk.datamanagement.service.IDataMasking;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_MASKING})
@RestController
@RequestMapping("/DataMasking")
public class DataMaskingController {

    @Resource
    IDataMasking service;

    @ApiOperation("获取数据源配置")
    @PostMapping("/getSourceDataConfig")
    public ResultEntity<DataMaskingTargetDTO> getSourceDataConfig(@Validated @RequestBody DataMaskingSourceDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSourceDataConfig(dto));
    }

    @ApiOperation("根据guid获取表信息")
    @PostMapping("/getTableData")
    public ResultEntity<Object> getTableData(@Validated @RequestBody SourceTableDataDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableData(dto));
    }

}
