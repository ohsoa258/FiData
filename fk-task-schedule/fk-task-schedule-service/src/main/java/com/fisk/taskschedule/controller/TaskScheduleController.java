package com.fisk.taskschedule.controller;

/**
 * @author Lock
 */

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.taskschedule.dto.TaskCronDTO;
import com.fisk.taskschedule.dto.TaskScheduleDTO;
import com.fisk.taskschedule.service.ITaskSchedule;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/taskSchedule")
@Slf4j
public class TaskScheduleController {

    @Resource
    private ITaskSchedule service;

    @ApiOperation("添加")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody TaskScheduleDTO dto) {

        ResultEntity<TaskCronDTO> result = service.addData(dto);

        log.info("方法的执行结果为:{}", result);
        // TODO 提供给task模块


        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    @ApiOperation("修改")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody TaskScheduleDTO dto) {

        ResultEntity<TaskCronDTO> result = service.editData(dto);
        log.info("方法的执行结果为:{}", result);
        // TODO 提供给task模块


        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    @ApiOperation("查询")
    @PostMapping("/get")
    public ResultEntity<Object> getData(@RequestBody TaskScheduleDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(dto));
    }


}
