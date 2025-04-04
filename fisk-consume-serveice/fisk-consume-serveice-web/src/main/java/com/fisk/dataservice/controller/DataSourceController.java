package com.fisk.dataservice.controller;

import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.datasource.DataSourceConDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConQuery;
import com.fisk.dataservice.dto.datasource.DataSourceConEditDTO;
import com.fisk.dataservice.dto.datasource.TestConnectionDTO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.dataservice.service.IDataSourceConManageService;
import com.fisk.dataservice.vo.datasource.DataSourceVO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据源控制器
 * @date 2022/1/6 14:51
 */

@Api(tags = {SwaggerConfig.TAG_1})
@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Resource
    private IDataSourceConManageService service;

    @PostMapping("/page")
    @ApiOperation("获取所有数据源连接信息")
    public ResultEntity<PageDTO<DataSourceConVO>> getData(@RequestBody DataSourceConQuery query) {
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

    @GetMapping("/getAll")
    @ApiOperation("获取所有数据源连接信息")
    public ResultEntity<List<DataSourceConVO>> getAll() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll());
    }

    @GetMapping("/reloadDataSource/{id}")
    @ApiOperation("重新加载数据源到redis")
    public ResultEntity<Object> reloadDataSource(@PathVariable("id")int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.reloadMetaData(id));
    }

    @GetMapping("/getMetaDataById/{id}")
    @ApiOperation("获取Api配置页面左侧Tree元数据信息")
    public ResultEntity<DataSourceVO> getFiDataConfigMetaData(@PathVariable("id")int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getMetaDataById(id));
    }

    @GetMapping("/getApiCustomDataSource")
    @ApiOperation("获取API自定义数据集")
    public ResultEntity<List<DataSourceDTO>> getApiCustomDataSource() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getApiCustomDataSource());
    }


}
