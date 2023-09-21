package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.v3.SourceColumnMetaQueryDTO;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_7})
@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Resource
    IAppDataSource service;

    @ApiOperation(value = "根据appId获取所有数据源以及数据库、表数据")
    @GetMapping("/getDataSourceMeta/{appId}")
    public ResultEntity<Object> getDataSourceMeta(@PathVariable long appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourceMeta(appId));
    }

    /**
     * 数据接入，刷新redis里存储的表信息
     *
     * @param appId
     * @return
     */
    @ApiOperation(value = "数据接入，刷新redis里存储的表信息")
    @GetMapping("/refreshRedis/{appId}")
    public ResultEntity<Object> refreshRedis(@PathVariable long appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.refreshRedis(appId));
    }
//
//    @ApiOperation(value = "根据数据源id重新加载所有数据源以及数据库、表数据")
//    @GetMapping("/setDataSourceMeta/{appId}")
//    public ResultEntity<Object> setDataSourceMeta(@PathVariable long appId) {
//        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setDataSourceMetaV2(appId));
//    }

    @PostMapping("/getDatabaseNameList")
    @ApiOperation(value = "根据服务配置信息,获取所有的数据库名称")
    public ResultEntity<Object> getDatabaseNameList(@RequestBody AppDataSourceDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDatabaseNameList(dto));
    }

    @PostMapping("/getSourceColumnMeta")
    @ApiOperation(value = "根据表名称获取字段信息")
    public ResultEntity<Object> getSourceColumnMeta(@RequestBody SourceColumnMetaQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSourceColumnMeta(dto));
    }

    @ApiOperation(value = "根据appId获取所有数据源")
    @GetMapping("/getDataSourcesByAppId/{appId}")
    public ResultEntity<Object> getDataSourcesByAppId(@PathVariable Integer appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourcesByAppId(appId));
    }

    @ApiOperation(value = "根据数据源类型获取平台配置模块的外部数据源")
    @GetMapping("/getOutSources/{driverType}")
    public ResultEntity<Object> getOutSourcesFromSystemConfigByTypeId(@PathVariable("driverType") String driverType) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getOutDataSourcesByTypeId(driverType));
    }

    @ApiOperation(value = "根据数据源id获取单个平台配置模块的外部数据源详情")
    @GetMapping("/getOutSource/{driverId}")
    public ResultEntity<DataSourceDTO> getOutSourceFromSystemConfigById(@PathVariable("driverId") Integer id) {
        return service.getOutSourceById(id);
    }

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 配合task模块，当平台配置修改数据源信息时，数据接入引用的数据源信息一并修改
     *
     * @param dto
     * @return
     */
    @ApiOperation(value = "修改数据接入引用的平台配置数据源信息")
    @PostMapping("/editDataSourceByTask")
    public ResultEntity<Boolean> editDataSourceByTask(@RequestBody DataSourceSaveDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.editDataSource(dto));
    }

    /**
     * 仅供task模块远程调用--引用需谨慎！
     * 根据SystemDataSourceId获取数据接入引用的数据源信息
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "根据SystemDataSourceId获取数据接入引用的数据源信息")
    @GetMapping("/getDataSourcesBySystemDataSourceId")
    public ResultEntity<List<AppDataSourceDTO>> getDataSourcesBySystemDataSourceId(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourcesBySystemDataSourceId(id));
    }

    /**
     * 获取数据接入引用的数据源id
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取数据接入引用的数据源id")
    @GetMapping("/getAccessDataSources")
    public ResultEntity<AppDataSourceDTO> getAccessDataSources(@RequestParam("id") Long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAccessDataSources(id));
    }

    /**
     * 仅供数据资产模块调用使用,通过tb_app_datasource表内数据的id获取系统模块对应的数据源的信息
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "仅供数据资产模块调用使用,通过tb_app_datasource表内数据的id获取系统模块对应的数据源的信息")
    @GetMapping("/getSystemDataSourceById")
    public ResultEntity<DataSourceDTO> getSystemDataSourceById(@RequestParam("id") Integer id) {
        return service.getSystemDataSourceById(id);
    }

}
