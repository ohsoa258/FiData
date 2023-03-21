package com.fisk.apiservice.controller;

import com.fisk.apiservice.config.SwaggerConfig;
import com.fisk.apiservice.dto.dataservice.TokenDTO;
import com.fisk.apiservice.service.IDataServiceManageService;
import com.fisk.common.core.response.ResultEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.AUTH_CONTROLLER})
@RestController
@RequestMapping("/authService")
@EnableAsync
public class AuthServiceController {
    @Resource
    private IDataServiceManageService service;

    @ApiOperation("获取token")
    @PostMapping("/getToken")
    public ResultEntity<Object> getToken(@RequestBody TokenDTO dto)
    {
        return service.getToken(dto);
    }
}
