package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.datasource.DataSourceColumnQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
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


}
