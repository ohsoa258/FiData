package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.SystemVersionDTO;
import com.fisk.system.service.SystemVersionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 56263
 * @version 1.0
 * @createTime 2023-04-7 14:40
 * @description 平台版本信息控制器
 */
@Api(tags = {SwaggerConfig.SYSTEM_VERSION_CONTROLLER})
@RestController
@RequestMapping("/version")
@Slf4j
public class SystemVersionController {

    @Resource
    private SystemVersionService service;

    @GetMapping("/get")
    @ApiOperation("获得当前平台最新的版本信息")
    public ResultEntity<SystemVersionDTO> get() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.get());
    }

}
