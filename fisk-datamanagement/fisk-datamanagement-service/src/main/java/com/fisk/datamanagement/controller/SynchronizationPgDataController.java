package com.fisk.datamanagement.controller;

import com.fisk.datamanagement.synchronization.fidata.SynchronizationData;
import com.fisk.datamanagement.synchronization.fidata.SynchronizationKinShip;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/SynchronizationPgData")
public class SynchronizationPgDataController {

    @Resource
    SynchronizationData service;
    @Resource
    SynchronizationKinShip ship;

    @ApiOperation("获取业务元数据列表")
    @GetMapping("/getPgData")
    public void getPgData() {
        service.synchronizationPgData();
    }

    @ApiOperation("获取业务元数据血缘")
    @GetMapping("/getShip")
    public void getShip() {
        ship.synchronizationKinShip();
    }

}
