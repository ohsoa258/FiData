package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.SYNCHRONIZATION_DATA})
@RestController
@Slf4j
@RequestMapping("/BloodCompensation")
public class BloodCompensationController {

    @Resource
    IBloodCompensation service;

    @ApiOperation("初始化元数据")
    @GetMapping("/systemSynchronousBlood")
    public ResultEntity<Object> systemSynchronousBlood(
            @ApiParam(value = "执行账号", required = true)
            @RequestParam("currUserName") String currUserName,
            @ApiParam(value = "是否要初始化，1代表需要初始化，0代表不需要初始化", required = true)
            @RequestParam("initialization") int initialization,
            @ApiParam(value = "在不初始化时，可同步单个模块，空则同步所有模块。 1, 数据接入 2,数仓建模 3,API网关 4, 数据库分发服务 5, 数据分析试图服务 6, 主数据 7, 外部数据源")
            @RequestParam("moduleIds") List<Integer> moduleIds) {
        boolean initia = initialization >= 1;
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.systemSynchronousBlood(currUserName, initia, moduleIds));
    }

    @ApiOperation("新版本增量同步元数据")
    @GetMapping("/systemSynchronousBloodV2")
    public ResultEntity<Object> systemSynchronousBloodV2(
            @ApiParam(value = "可同步单个模块，空则同步所有模块。 1, 数据接入 2,数仓建模 3,API网关 4, 数据库分发服务 5, 数据分析试图服务 6, 主数据 7, 外部数据源")
            @RequestParam("moduleIds") List<Integer> moduleIds) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.systemSynchronousBloodV2(moduleIds));
    }

}
