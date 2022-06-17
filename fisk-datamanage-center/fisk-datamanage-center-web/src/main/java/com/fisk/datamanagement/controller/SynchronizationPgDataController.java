package com.fisk.datamanagement.controller;

import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.synchronization.fidata.SynchronizationData;
import com.fisk.datamanagement.synchronization.fidata.SynchronizationKinShip;
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
@RequestMapping("/SynchronizationData")
public class SynchronizationPgDataController {

    @Resource
    SynchronizationData service;
    @Resource
    SynchronizationKinShip ship;

    @ApiOperation("同步元数据对象")
    @GetMapping("/synchronizationData")
    public void synchronizationPgData() {
        service.synchronizationPgData();
    }

    @ApiOperation("同步元数据血缘")
    @GetMapping("/synchronizationKinShip")
    public void synchronizationKinShip() {
        ship.synchronizationKinShip();
    }

}
