package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.service.ApiFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2021/12/9 20:29
 */
@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/systemWebIndex")
public class SystemWebIndexController {

    @Resource
    private ApiFieldService configureFieldService;

    @ApiOperation(value = "获取数据服务系统数量")
    @GetMapping("/getServerApp")
    public ResultEntity<Long> getServerApp() {
        return configureFieldService.getAmount();
    }
}
