package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.datasource.DataSourceColumnQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceQueryDTO;
import com.fisk.dataservice.dto.tableservice.*;
import com.fisk.dataservice.service.IDataSourceConfig;
import com.fisk.dataservice.service.ITableAppManageService;
import com.fisk.dataservice.service.ITableService;
import com.fisk.dataservice.vo.tableservice.TableAppVO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.TAG_7})
@RestController
@RequestMapping("/tableService")
@EnableAsync
public class TableServiceController {

    @Resource
    ITableService service;
    @Resource
    ITableAppManageService tableAppManageService;
    @Resource
    IDataSourceConfig dataSourceConfig;
    @Resource
    DataFactoryClient dataFactoryClient;

    @ApiOperation(value = "应用过滤字段")
    @GetMapping("/getFilterColumn")
    public ResultEntity<Object> getFilterColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAppManageService.getFilterColumn());
    }

    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<Page<TableAppVO>> pageFilter(@RequestBody TableAppQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableAppManageService.pageFilter(dto));
    }

    @ApiOperation("添加应用")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody TableAppDTO dto) {
        return ResultEntityBuild.build(tableAppManageService.addData(dto));
    }

    @ApiOperation("编辑应用")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody TableAppDTO dto) {
        return ResultEntityBuild.build(tableAppManageService.editData(dto));
    }

    @ApiOperation("删除应用")
    @DeleteMapping("/delete/{tableAppId}")
    public ResultEntity<Object> deleteData(@PathVariable("tableAppId") int tableAppId) {
        return ResultEntityBuild.build(tableAppManageService.deleteData(tableAppId));
    }

    @ApiOperation("检查数据源是否没有被引用")
    @PostMapping("/checkDataSourceIsNoUse")
    public ResultEntity<Object> checkDataSourceIsNoUse(@RequestBody TableAppDatasourceDTO dto) {
        return ResultEntityBuild.build(tableAppManageService.checkDataSourceIsNoUse(dto));
    }

    @ApiOperation("分页获取表服务数据")
    @PostMapping("/getTableServiceListData")
    public ResultEntity<Object> getTableServiceListData(@RequestBody TableServicePageQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableServiceListData(dto));
    }

    @ApiOperation("获取FiData系统库表信息")
    @GetMapping("/getDbTableInfoList")
    public ResultEntity<Object> getDbTableInfoList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataSourceConfig.getTableInfoList());
    }

    @ApiOperation("获取表字段信息")
    @PostMapping("/getColumn")
    public ResultEntity<Object> getColumn(@RequestBody DataSourceColumnQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataSourceConfig.getColumn(dto));
    }

    @ApiOperation("根据自定义脚本查询数据")
    @PostMapping("/tableServiceQueryList")
    public ResultEntity<Object> tableServiceQueryList(@RequestBody DataSourceQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataSourceConfig.getTableServiceQueryList(dto));
    }

    @ApiOperation("新增表服务")
    @PostMapping("/addTableService")
    public ResultEntity<Object> addTableService(@RequestBody TableServiceDTO dto) {
        return service.addTableServiceData(dto);
    }

    @ApiOperation("获取自定义数据源配置")
    @GetMapping("/getDataSourceConfig")
    public ResultEntity<Object> getDataSourceConfig() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourceConfig());
    }

    @ApiOperation("获取平台所有数据源配置")
    @GetMapping("/getAllDataSourceConfig")
    public ResultEntity<Object> getAllDataSourceConfig() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllDataSourceConfig());
    }

    @ApiOperation("表服务配置保存")
    @PostMapping("/TableServiceSave")
    public ResultEntity<Object> TableServiceSave(@RequestBody TableServiceSaveDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.TableServiceSave(dto));
    }

    @ApiOperation("获取表配置详情")
    @GetMapping("/getTableServiceById/{id}")
    public ResultEntity<Object> getTableServiceById(@PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableServiceById(id));
    }

    @ApiOperation("删除表服务配置")
    @DeleteMapping("/getTableServiceById/{id}")
    public ResultEntity<Object> delTableServiceById(@PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.delTableServiceById(id));
    }

    @ApiOperation("获取所有管道")
    @GetMapping("/getNifiCustomWorkFlowDrop")
    public ResultEntity<Object> getNifiCustomWorkFlowDrop() {
        return dataFactoryClient.getNifiCustomWorkFlowDrop();
    }

    @ApiOperation("获取库中表集合")
    @GetMapping("/getAllTableByDb/{id}")
    public ResultEntity<Object> getAllTableByDb(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataSourceConfig.getAllTableByDb(id));
    }

    @ApiOperation("根据库和表获取字段集合")
    @GetMapping("/getColumnByTable")
    public ResultEntity<Object> getColumnByTable(Integer id, String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataSourceConfig.getColumnByTable(id, tableName));
    }

    @ApiOperation("根据管道id获取表服务集合")
    @GetMapping("/getTableListByPipelineId/{id}")
    public ResultEntity<List<BuildTableServiceDTO>> getTableListByPipelineId(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableListByPipelineId(id));
    }

    @ApiOperation("修改表服务发布状态")
    @PutMapping("/updateTableServiceStatus")
    public void updateTableServiceStatus(@RequestBody TableServicePublishStatusDTO dto) {
        service.updateTableServiceStatus(dto);
    }

    @ApiOperation("根据表服务id构建发布数据")
    @GetMapping("/getBuildTableServiceById/{id}")
    public ResultEntity<BuildTableServiceDTO> getBuildTableServiceById(@PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBuildTableServiceById(id));
    }

}
