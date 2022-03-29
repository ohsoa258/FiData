package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConEditDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConQuery;
import com.fisk.datagovernance.dto.dataquality.datasource.TestConnectionDTO;
import com.fisk.datagovernance.enums.dataquality.ModuleDataSourceTypeEnum;
import com.fisk.datagovernance.service.dataquality.IDataSourceConManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据源配置
 * @date 2022/3/22 16:14
 */
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Resource
    private IDataSourceConManageService service;

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
    @ApiOperation("获取全部表字段信息")
    public ResultEntity<List<DataSourceVO>> getTableAll(String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMeta(tableName));
    }

    @GetMapping("/getTableFieldAll")
    @ApiOperation("获取表字段信息")
    public ResultEntity<DataSourceVO> getTableFieldAll(int datasourceId, ModuleDataSourceTypeEnum datasourceTyoe,
                                                       String tableName,String tableFramework) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableFieldAll(datasourceId,datasourceTyoe,tableName,tableFramework));
    }
}
