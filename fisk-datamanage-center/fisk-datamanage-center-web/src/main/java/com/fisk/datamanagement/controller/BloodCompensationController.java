package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.SYNCHRONIZATION_DATA})
@RestController
@RequestMapping("/BloodCompensation")
public class BloodCompensationController {

    @Resource
    IBloodCompensation service;

    @ApiOperation("同步元数据")
    @GetMapping("/systemSynchronousBlood")
    public ResultEntity<Object> systemSynchronousBlood() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.systemSynchronousBlood());
    }

}
