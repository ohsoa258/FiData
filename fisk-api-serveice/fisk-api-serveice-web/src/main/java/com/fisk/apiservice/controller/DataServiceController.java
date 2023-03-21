package com.fisk.apiservice.controller;

import com.fisk.apiservice.config.SwaggerConfig;
import com.fisk.apiservice.dto.dataservice.RequstDTO;
import com.fisk.apiservice.service.IDataServiceManageService;
import com.fisk.common.core.response.ResultEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.DATASERVICE_CONTROLLER})
@RestController
@RequestMapping("/dataService")
@EnableAsync
public class DataServiceController {
    @Resource
    private IDataServiceManageService service;

    @ApiOperation("获取数据")
    @PostMapping("/getData")
    public ResultEntity<Object> getData(@Validated @RequestBody RequstDTO dto)
    {
        return service.getData(dto);
    }
}
