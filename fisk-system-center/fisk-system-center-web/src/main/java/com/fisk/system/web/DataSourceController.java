package com.fisk.system.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourceQueryDTO;
import com.fisk.system.dto.datasource.DataSourceResultDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.system.service.IDataSourceManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description FiData数据源控制器
 * @date 2022/6/13 14:51
 */

@Api(tags = {SwaggerConfig.DATASOURCE})
@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Resource
    private IDataSourceManageService service;

    @GetMapping("/getById/{datasourceId}")
    @ApiOperation("获取单条数据源连接信息")
    public ResultEntity<DataSourceDTO> getById(@RequestParam("datasourceId") int datasourceId) {
        return service.getById(datasourceId);
    }

    @PostMapping("/getAll")
    @ApiOperation("外部接口，获取所有数据源连接信息")
    public ResultEntity<List<DataSourceDTO>> getAll() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll());
    }

    @PostMapping("/getAllFiDataDataSource")
    @ApiOperation("外部接口，获取系统数据源连接信息")
    public ResultEntity<List<DataSourceDTO>> getAllFiDataDataSource() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSystemDataSource());
    }

    @PostMapping("/getAllExternalDataSource")
    @ApiOperation("外部接口，获取外部数据源连接信息")
    public ResultEntity<List<DataSourceDTO>> getAllExternalDataSource() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getExternalDataSource());
    }

    @GetMapping("/getSearchColumn")
    @ApiOperation(value = "获取搜索条件字段")
    public ResultEntity<Object> getSearchColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSearchColumn());
    }

    @PostMapping("/getAllDataSource")
    @ApiOperation("获取所有数据源连接信息")
    public ResultEntity<Page<DataSourceDTO>> getAllDataSource(@RequestBody DataSourceQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllDataSource(dto));
    }

    @PostMapping("/add")
    @ApiOperation("添加数据源")
    public ResultEntity<Object> addDate(@RequestBody DataSourceSaveDTO dto) {
        return ResultEntityBuild.build(service.insertDataSource(dto));
    }

    @PutMapping("/edit")
    @ApiOperation("编辑数据源")
    public ResultEntity<Object> editData(@RequestBody DataSourceSaveDTO dto) {
        return ResultEntityBuild.build(service.updateDataSource(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除数据源")
    public ResultEntity<Object> deleteDate(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteDataSource(id));
    }

    @PostMapping("/test")
    @ApiOperation("测试数据源连接")
    public ResultEntity<Object> testConnection(@RequestBody DataSourceSaveDTO dto) {
        return ResultEntityBuild.build(service.testConnection(dto));
    }

    @PostMapping("/insertDataSourceByAccess")
    @ApiOperation("同步数据接入数据源")
    public ResultEntity<DataSourceResultDTO> insertDataSourceByAccess(@RequestBody DataSourceSaveDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.insertDataSourceByAccess(dto));
    }

}
