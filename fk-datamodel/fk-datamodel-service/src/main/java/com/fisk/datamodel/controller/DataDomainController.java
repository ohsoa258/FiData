package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.DataDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2021/8/12 11:29
 */
@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/Datamation")
public class DataDomainController {

    @Resource
    private DataDomainService domainService;

    @ApiOperation("获取数据域")
    @GetMapping("/getAll")
    public ResultEntity<Object> getAll() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, domainService.getDataDomain());
    }

    @ApiOperation("获取维度")
    @GetMapping("/getDimension")
    public ResultEntity<Object> getDimension() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, domainService.getDimension());
    }

    @ApiOperation("获取业务板块")
    @GetMapping("/getBusiness")
    public ResultEntity<Object> getBusiness() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, domainService.getBusiness());
    }
}
