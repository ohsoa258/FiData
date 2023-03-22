package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.service.IATVServiceAnalyseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:17
 * @description Api && Table && View 服务数据分析 Controller
 */
@Api(tags = {SwaggerConfig.TAG_8})
@RestController
@RequestMapping("/ServiceAnalyse")
public class ATVServiceAnalyseController {
    @Resource
    private IATVServiceAnalyseService analyseService;

    @ApiOperation(value = "Api && Table && View服务数据报表分析")
    @GetMapping("/getServiceanalysis")
    public ResultEntity<Object> getServiceanalysis() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getServiceAnalyse());
    }
}
