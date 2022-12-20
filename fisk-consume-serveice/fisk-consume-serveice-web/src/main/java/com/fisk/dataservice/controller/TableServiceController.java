package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.datasource.DataSourceColumnQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceSaveDTO;
import com.fisk.dataservice.service.IDataSourceConfig;
import com.fisk.dataservice.service.ITableService;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/tableService")
@EnableAsync
public class TableServiceController {

    @Resource
    ITableService service;
    @Resource
    IDataSourceConfig dataSourceConfig;

    @ApiOperation("分页获取表服务数据")
    @PostMapping("/getTableServiceListData")
    public ResultEntity<Object> getTableServiceListData(@RequestBody TableServicePageQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableServiceListData(dto));
    }

    @ApiOperation("获取fidata系统库表信息")
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

    @ApiOperation("表服务配置保存")
    @PostMapping("/TableServiceSave")
    public ResultEntity<Object> TableServiceSave(@RequestBody TableServiceSaveDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.TableServiceSave(dto));
    }


}
