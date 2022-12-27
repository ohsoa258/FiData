package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.dataservice.dto.datasource.DataSourceColumnQueryDTO;
import com.fisk.dataservice.dto.datasource.DataSourceQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceSaveDTO;
import com.fisk.dataservice.service.IDataSourceConfig;
import com.fisk.dataservice.service.ITableService;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    @Resource
    DataFactoryClient dataFactoryClient;

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

    @ApiOperation("获取库和表获取字段集合")
    @GetMapping("/getColumnByTable")
    public ResultEntity<Object> getColumnByTable(Integer id, String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataSourceConfig.getColumnByTable(id, tableName));
    }

    @ApiOperation("根据管道id获取表服务集合")
    @GetMapping("/getTableListByPipelineId/{id}")
    public ResultEntity<List<BuildTableServiceDTO>> getTableListByPipelineId(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableListByPipelineId(id));
    }

}
