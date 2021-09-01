package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsDTO;
import com.fisk.datamodel.dto.factsyncmode.FactSyncModeDTO;
import com.fisk.datamodel.service.IFactSyncMode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.FACT_SYNC_MODE})
@RestController
@RequestMapping("/FactSyncMode")
@Slf4j
public class FactSyncModeController {

    @Resource
    IFactSyncMode service;

    @ApiOperation("添加事实表同步方式")
    @PostMapping("/addFactSyncMode")
    public ResultEntity<Object> addAtomicIndicators(@Validated @RequestBody FactSyncModeDTO dto) {
        return ResultEntityBuild.build(service.addFactSyncMode(dto));
    }

    @ApiOperation("根据事实表id获取同步方式详情")
    @GetMapping("/getFactSyncMode/{id}")
    public ResultEntity<Object> getFactSyncMode(@PathVariable("id") int factId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactSyncMode(factId));
    }

    @ApiOperation("修改事实表同步方式")
    @PutMapping("/updateFactSyncMode")
    public ResultEntity<Object> updateFactSyncMode(@Validated @RequestBody FactSyncModeDTO dto) {
        return ResultEntityBuild.build(service.updateFactSyncMode(dto));
    }

    @ApiOperation("根据同步方式id获取相关数据(用于NIFI创建流程)")
    @GetMapping("/factSyncModePush/{id}")
    public ResultEntity<Object> factSyncModePush(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.factSyncModePush(id));
    }

}
