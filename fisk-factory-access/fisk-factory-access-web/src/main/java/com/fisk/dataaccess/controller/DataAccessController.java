package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.dataaccess.service.IDataAccess;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/dataAccessTree")
public class DataAccessController {

    @Resource
    private ITableAccess tableAccessService;

    @Resource
    private IDataAccess service;

    @ApiOperation("应用注册tree")
    @GetMapping("/getTree")
    public ResultEntity<Object> getTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessService.getTree());
    }

    @ApiOperation("获取表Id")
    @PostMapping("/getTableId")
    public ResultEntity<List<ChannelDataDTO>> getTableId(@RequestBody NifiComponentsDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessService.getTableId(dto));
    }
    @ApiOperation("获取表Id")
    @GetMapping("/getTableId")
    public ResultEntity<List<ChannelDataDTO>> getTableId() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAccessService.getTableId());
    }

    @ApiOperation("根据appId和tableId 获取appName和tableName")
    @PostMapping("/getAppNameAndTableName")
    public ResultEntity<Object> getAppNameAndTableName(@RequestBody DataAccessIdsDTO dto) {
        ResultEntity<ComponentIdDTO> result = tableAccessService.getAppNameAndTableName(dto);
        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    @ApiOperation("获取数据接入已发布的元数据对象")
    @GetMapping("/getDataAccessMetaData")
    public ResultEntity<List<DataAccessSourceTableDTO>> getDataAccessMetaData() {
        return service.getDataAccessMetaData();
    }

    @ApiOperation("构建元数据查询对象(表及下面的字段)")
    @PostMapping("/buildFiDataTableMetaData")
    public ResultEntity<List<FiDataTableMetaDataDTO>> buildFiDataTableMetaData(@RequestBody FiDataTableMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.buildFiDataTableMetaData(dto));
    }
}
