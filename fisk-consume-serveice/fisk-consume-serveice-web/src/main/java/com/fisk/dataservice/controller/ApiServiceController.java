package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.apiservice.RequstDTO;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.service.IApiServiceManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author dick
 * @version v1.0
 * @description api应用服务控制器
 * @date 2022/1/6 14:51
 */

@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/apiService")
@EnableAsync
public class ApiServiceController {
    @Resource
    private IApiServiceManageService service;

    @ApiOperation("获取token")
    @PostMapping("/getToken")
    public ResultEntity<Object> getToken(@RequestBody TokenDTO dto)
    {
        return service.getToken(dto);
    }

    @ApiOperation("获取数据")
    @PostMapping("/getData")
    public ResultEntity<Object> getData(@Validated @RequestBody RequstDTO dto)
    {
        return service.getData(dto);
    }
}
