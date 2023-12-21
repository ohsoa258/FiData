package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.service.monitor.AccessLakeMonitorService;
import com.fisk.datagovernance.vo.monitor.AccessLakeMonitorVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Api(tags = SwaggerConfig.ACCESS_LAKE_MONITOR)
@RestController
@RequestMapping("/AccessLikeMonitor")
@Slf4j
public class AccessLikeMonitorController {

    @Resource
    AccessLakeMonitorService accessLAkeMonitorService;

    @ApiOperation("获取数据湖入湖监控参数")
    @GetMapping("/getAccessLakeMonitor")
    public ResultEntity<AccessLakeMonitorVO> getAccessLakeMonitor(@RequestParam("ip") Integer appId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessLAkeMonitorService.getAccessLakeMonitor(appId));
    }
}
