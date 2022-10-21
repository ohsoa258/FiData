package com.fisk.datafactory.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.taskdatasourceconfig.TaskDataSourceConfigDTO;
import com.fisk.datafactory.service.ITaskDataSourceConfig;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/TaskDataSourceConfig")
public class TaskDataSourceConfigController {

    @Resource
    private ITaskDataSourceConfig service;

    @GetMapping("/getDrive")
    @ApiOperation(value = "驱动列表")
    public ResultEntity<Object> getDrive() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDrive());
    }

    @PostMapping("/testConnection")
    @ApiOperation(value = "测试连接")
    public ResultEntity<Object> testConnection(@RequestBody TaskDataSourceConfigDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.testConnection(dto));
    }

    @PostMapping("/addOrUpdateTaskDataSourceConfig")
    @ApiOperation(value = "新增或修改任务数据源配置")
    public ResultEntity<Object> addOrUpdateTaskDataSourceConfig(@RequestBody TaskDataSourceConfigDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addOrUpdateTaskDataSourceConfig(dto));
    }

    @GetMapping("/getTaskDataSourceConfig")
    @ApiOperation(value = "任务数据源配置详情")
    public ResultEntity<Object> getTaskDataSourceConfig(Integer taskId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTaskDataSourceConfig(taskId));
    }

}
