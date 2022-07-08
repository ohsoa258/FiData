package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
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
    @ApiOperation("数据质量，获取所有数据源配置信息")
    public ResultEntity<PageDTO<DataSourceConVO>> page(@RequestBody DataSourceConQuery query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.page(query));
    }

    @PostMapping("/add")
    @ApiOperation("数据质量，添加数据源配置信息")
    public ResultEntity<Object> add(@Validated @RequestBody DataSourceConDTO dto) {
        return ResultEntityBuild.build(service.add(dto));
    }

    @PutMapping("/edit")
    @ApiOperation("数据质量，编辑数据源配置信息")
    public ResultEntity<Object> edit(@Validated @RequestBody DataSourceConEditDTO dto) {
        return ResultEntityBuild.build(service.edit(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("数据质量，删除数据源配置信息")
    public ResultEntity<Object> delete(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.delete(id));
    }

    @PostMapping("/test")
    @ApiOperation("数据质量，测试数据源连接")
    public ResultEntity<Object> testConnection(@Validated @RequestBody TestConnectionDTO dto) {
        return ResultEntityBuild.build(service.testConnection(dto));
    }

    @GetMapping("/getFiDataConfigMetaData")
    @ApiOperation("数据质量，获取FiData配置表元数据信息")
    public ResultEntity<FiDataMetaDataTreeDTO> getFiDataConfigMetaData() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFiDataConfigMetaData());
    }

    @GetMapping("/getCustomizeMetaData")
    @ApiOperation("数据质量，获取自定义数据源表元数据信息")
    public ResultEntity<FiDataMetaDataTreeDTO> getCustomizeMetaData() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCustomizeMetaData());
    }

    @PostMapping("/getDataOpsDataSource")
    @ApiOperation("数据运维，获取数据运维数据源信息")
    public ResultEntity<List<DataOpsSourceVO>> getDataOpsDataSource() {
        return dataOpsDataSourceManageService.getDataOpsDataSource();
    }

    @PostMapping("/reloadDataOpsDataSource")
    @ApiOperation("数据运维，pg数据库信息同步到redis")
    public ResultEntity<Object> reloadDataOpsDataSource() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataOpsDataSourceManageService.reloadDataOpsDataSource());
    }

    @PostMapping("/executeDataOpsSql")
    @ApiOperation("数据运维，执行sql")
    public ResultEntity<ExecuteResultVO> executeDataOpsSql(@Validated @RequestBody ExecuteDataOpsSqlDTO dto) {
        return dataOpsDataSourceManageService.executeDataOpsSql(dto);
    }

}
