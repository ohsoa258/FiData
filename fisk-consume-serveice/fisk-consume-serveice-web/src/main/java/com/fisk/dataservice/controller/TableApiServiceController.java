package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.tableapi.*;
import com.fisk.dataservice.dto.tableservice.TableServicePublishStatusDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceSyncDTO;
import com.fisk.dataservice.service.ITableApiLogService;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.dataservice.service.ITableAppManageService;
import com.fisk.dataservice.service.impl.AsyncImpl;
import com.fisk.dataservice.vo.tableapi.ConsumeServerVO;
import com.fisk.dataservice.vo.tableapi.TopFrequencyVO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@Api(tags = {SwaggerConfig.TAG_11})
@RestController
@RequestMapping("/tableApiService")
public class TableApiServiceController {
    @Resource
    private ITableApiService tableApiService;

    @Resource
    ITableApiLogService tableApiLogService;

    @Resource
    private AsyncImpl async;

    @ApiOperation("分页获取数据分发服务Api数据")
    @PostMapping("/getTableApiListData")
    public ResultEntity<Object> getTableApiListData(@RequestBody TableApiPageQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableApiService.getTableApiListData(dto));
    }

    @ApiOperation("根据管道id获取数据分发api集合")
    @GetMapping("/getTableApiListByPipelineId/{id}")
    public ResultEntity<List<BuildTableApiServiceDTO>> getTableApiListByPipelineId(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableApiService.getTableApiListByPipelineId(id));
    }

    @ApiOperation("根据接入id获取数据分发api集合")
    @GetMapping("/getTableApiListByInputId/{id}")
    public ResultEntity<List<BuildTableApiServiceDTO>> getTableApiListByInputId(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableApiService.getTableApiListByInputId(id));
    }

    @ApiOperation("获取api配置详情")
    @GetMapping("/getApiServiceById/{apiId}")
    public ResultEntity<Object> getApiServiceById(@PathVariable("apiId") long apiId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableApiService.getApiServiceById(apiId));
    }

    @ApiOperation("新增数据分发服务Api")
    @PostMapping("/addTableApiService")
    public ResultEntity<Object> addTableApiService(@RequestBody TableApiServiceDTO dto) {
        return tableApiService.addTableApiService(dto);
    }

    @ApiOperation("数据分发服务Api配置保存")
    @PostMapping("/TableApiServiceSave")
    public ResultEntity<Object> TableApiServiceSave(@RequestBody TableApiServiceSaveDTO dto) {
        return ResultEntityBuild.build(tableApiService.TableApiServiceSave(dto));
    }

    @ApiOperation("删除数据分发服务Api服务配置")
    @DeleteMapping("/delTableApiById/{id}")
    public ResultEntity<Object> delTableApiById(@PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableApiService.delTableApiById(id));
    }

    @ApiOperation("修改数据分发服务Api服务发布状态")
    @PutMapping("/updateTableApiStatus")
    public void updateTableApiStatus(@RequestBody TableServicePublishStatusDTO dto) {
        tableApiService.updateTableServiceStatus(dto);
    }

    @ApiOperation("NIFI执行api同步（同步中）")
    @PostMapping("/syncTableApi")
    @Async
    public ResultEntity<Object> syncTableApi(@Validated @RequestBody TableApiSyncDTO dto) {
        async.syncTableApi(dto);
        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    @ApiOperation("数据库同步服务-新增同步按钮,手动同步表服务")
    @PostMapping("/editTableServiceSync")
    public ResultEntity<Object> editTableServiceSync(@RequestBody TableApiServiceSyncDTO tableServiceSyncDTO) {
        return ResultEntityBuild.build(tableApiService.editTableApiServiceSync(tableServiceSyncDTO));
    }

    @ApiOperation("表服务启用或禁用")
    @GetMapping("/enableOrDisable")
    public ResultEntity<List<String>> enableOrDisable(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(tableApiService.enableOrDisable(id));
    }

    @ApiOperation("获取数据消费(当天)")
    @GetMapping("/getConsumeServer")
    public ResultEntity<ConsumeServerVO> getConsumeServer() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,tableApiLogService.getConsumeServer());
    }

    @ApiOperation("获取输出接口使用频率top5")
    @GetMapping("/getTopFrequency")
    public ResultEntity<List<TopFrequencyVO>> getTopFrequency() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,tableApiLogService.getTopFrequency());
    }

    @ApiOperation("重点接口标记")
    @GetMapping("/importantOrUnimportant")
    public ResultEntity<List<String>> importantOrUnimportant(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(tableApiService.importantOrUnimportant(id));
    }
}
