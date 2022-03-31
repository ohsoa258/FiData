package com.fisk.chartvisual.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.dto.DataSourceConQuery;
import com.fisk.chartvisual.dto.TestConnectionDTO;
import com.fisk.chartvisual.service.IDataSourceConManageService;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据源管理
 *
 * @author gy
 */
@RestController
@RequestMapping("/dscon")
public class DataSourceConManageController {

    @Resource
    private IDataSourceConManageService service;

    @GetMapping("/page")
    @ApiOperation("获取所有数据源连接信息")
    public ResultEntity<Page<DataSourceConVO>> getData(Page<DataSourceConVO> page, DataSourceConQuery query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listDataSourceCons(page, query));
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

    @DeleteMapping("/delete")
    @ApiOperation("删除数据源连接信息")
    public ResultEntity<Object> deleteData(int id) {
        return ResultEntityBuild.build(service.deleteDataSourceCon(id));
    }

    @PostMapping("/test")
    @ApiOperation("测试数据源连接")
    public ResultEntity<Object> testConnection(@Validated @RequestBody TestConnectionDTO dto) {
        return ResultEntityBuild.build(service.testConnection(dto));
    }

    @GetMapping("/getDataDomain")
    @ApiOperation("根据数据源连接获取数据域")
    public ResultEntity<List<DataDomainVO>> getDataDomain(int id) {
        return service.listDataDomain(id);
    }

    @GetMapping("/getSSASDataStructure")
    @ApiOperation("根据数据源连接获取数据域")
    public ResultEntity<List<DataDomainVO>> getSSASDataStructure(int id) {
        return service.SSASDataStructure(id);
    }
}
