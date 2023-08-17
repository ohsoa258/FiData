package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.azure.QueryData;
import com.fisk.datamanagement.service.AzureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2023-08-07
 * @Description:
 */
@Slf4j
@Api(tags = {SwaggerConfig.AZURE_SERVER})
@RestController
@RequestMapping("/AzureServer")
public class AzureServerController {

    @Resource
    AzureService azureService;
    @ApiOperation("获取数据")
    @PostMapping("/getData")
    public ResultEntity<List<Map<String,Object>>> getData(@RequestBody QueryData queryData) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,azureService.getData(queryData));
    }
    @ApiOperation("根据chat返回结果获取数据")
    @PostMapping("/getSelectChatData")
    public ResultEntity<List<Map<String,Object>>> getSelectChatData(@RequestBody QueryData queryData) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,azureService.getSelectChatData(queryData));
    }
}

