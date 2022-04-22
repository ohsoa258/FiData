package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.dto.dataops.PostgreDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.*;
import com.fisk.datagovernance.service.dataops.IDataOpsDataSourceManageService;
import com.fisk.datagovernance.service.dataquality.IDataSourceConManageService;
import com.fisk.datagovernance.vo.dataops.DataOpsSourceVO;
import com.fisk.datagovernance.vo.dataops.DataOpsTableFieldVO;
import com.fisk.datagovernance.vo.dataops.ExecuteResultVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataExampleSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据源配置
 * @date 2022/3/22 16:14
 */
@Api(tags = {SwaggerConfig.DATASOURCE_CONTROLLER})
@RestController
@RequestMapping("/datasource")
public class DataSourceController {

    @Resource
    private IDataSourceConManageService service;

    @Resource
    private IDataOpsDataSourceManageService dataOpsDataSourceManageService;

    @PostMapping("/page")
    @ApiOperation("获取所有数据源连接信息")
    public ResultEntity<Page<DataSourceConVO>> getData(@RequestBody DataSourceConQuery query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listDataSourceCons(query));
    }

    @PostMapping("/add")
    @ApiOperation("添加数据源连接信息")
    public ResultEntity<Object> addData(@Validated @RequestBody DataSourceConDTO dto) {
        return ResultEntityBuild.build(service.saveDataSourceCon(dto));
    }

    @PutMapping("/edit")
    @ApiOperation("编辑数据源连接信息")
    public ResultEntity<Object> editData(@Validated @RequestBody DataSourceConEditDTO dto) {
        return ResultEntityBuild.build(service.updateDataSourceCon(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除数据源连接信息")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteDataSourceCon(id));
    }

    @PostMapping("/test")
    @ApiOperation("测试数据源连接")
    public ResultEntity<Object> testConnection(@Validated @RequestBody TestConnectionDTO dto) {
        return ResultEntityBuild.build(service.testConnection(dto));
    }

//    @GetMapping("/getAll")
//    @ApiOperation("获取所有数据源连接信息")
//    public ResultEntity<List<DataSourceConVO>> getAll() {
//        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll());
//    }

    @GetMapping("/getTableAll")
    @ApiOperation("获取全部表信息")
    public ResultEntity<List<DataExampleSourceVO>> getTableAll(String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMeta(tableName));
    }

    @PostMapping("/getTableFieldAll")
    @ApiOperation("获取表字段信息")
    public ResultEntity<DataSourceVO> getTableFieldAll(@Validated @RequestBody TableFieldQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableFieldAll(dto));
    }

    @PostMapping("/getDataOpsTableAll")
    @ApiOperation("获取数据源的实例信息")
    public ResultEntity<List<DataOpsSourceVO>> getDataOpsTableAll() {
        return dataOpsDataSourceManageService.getDataOpsTableAll();
    }

    @GetMapping("/getDataOpsTableFieldAll")
    @ApiOperation("获取数据源的表字段信息")
    public ResultEntity<List<DataOpsTableFieldVO>> getDataOpsTableFieldAll(int datasourceId, String tableName) {
        return dataOpsDataSourceManageService.getDataOpsTableFieldAll(datasourceId, tableName);
    }

    @PostMapping("/executeDataOpsSql")
    @ApiOperation("执行sql")
    public ResultEntity<ExecuteResultVO> executeDataOpsSql(@Validated @RequestBody ExecuteDataOpsSqlDTO dto) {
        return dataOpsDataSourceManageService.executeDataOpsSql(dto);
    }
}
