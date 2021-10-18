package com.fisk.taskfactory.controller;

/**
 * @author Lock
 */

import com.alibaba.fastjson.JSON;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.TableNifiSettingPO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.taskfactory.service.ITaskSchedule;
import com.fisk.taskschedule.dto.TaskCronDTO;
import com.fisk.taskschedule.dto.TaskScheduleDTO;
import com.fisk.taskschedule.dto.dataaccess.DataAccessIdDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@RequestMapping("/taskSchedule")
@Slf4j
public class TaskScheduleController {

    @Resource
    private ITaskSchedule service;
    @Resource
    private DataAccessClient client;
    @Resource
    PublishTaskClient publishTaskClient;

    @ApiOperation("添加")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody TaskScheduleDTO dto) {

        ResultEntity<TaskCronDTO> result = service.addData(dto);

        log.info("方法的执行结果为:{}", result);
        // TODO 提供给task模块
        TaskCronDTO taskCronDTO = result.data;
        DataAccessIdDTO dataAccessIdDTO = taskCronDTO.dto;
        if(Objects.equals(dataAccessIdDTO.olapTableEnum, OlapTableEnum.KPI)){
            ResultEntity<Object> objectResultEntity = publishTaskClient.selectByName(dataAccessIdDTO.factTableName);
            Long id = JSON.parseObject(JSON.toJSONString(objectResultEntity.data), Long.class);
            dataAccessIdDTO.tableId=id;
        }
        ResultEntity<TableNifiSettingPO> tableNifiSetting = publishTaskClient.getTableNifiSetting(dataAccessIdDTO);
        if (tableNifiSetting.code == 0) {
            taskCronDTO.dto.schedulerComponentId = tableNifiSetting.data.queryIncrementProcessorId;
            taskCronDTO.dto.tableComponentId = tableNifiSetting.data.tableComponentId;
            //publishTaskClient.modifyScheduling(taskCronDTO.dto.tableComponentId,taskCronDTO.dto.schedulerComponentId,taskCronDTO.dto.syncMode,taskCronDTO.dto.expression+" sec");"TIMER_DRIVEN"
            publishTaskClient.modifyScheduling(taskCronDTO.dto.tableComponentId,taskCronDTO.dto.schedulerComponentId,taskCronDTO.dto.syncMode,taskCronDTO.dto.expression);
        }
         return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    @ApiOperation("修改")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody TaskScheduleDTO dto) {

        ResultEntity<TaskCronDTO> result = service.editData(dto);
        log.info("方法的执行结果为:{}", result);
        // TODO 提供给task模块
        TaskCronDTO taskCronDTO = result.data;
        DataAccessIdDTO dataAccessIdDTO = taskCronDTO.dto;
        DataAccessIdsDTO accessIdsDTO = new DataAccessIdsDTO();
        accessIdsDTO.appId = taskCronDTO.dto.appId;
        accessIdsDTO.tableId = taskCronDTO.dto.tableId;
        ResultEntity<Object> clientComponentId = client.getComponentId(accessIdsDTO);
        ComponentIdDTO data = (ComponentIdDTO) clientComponentId.data;
        taskCronDTO.dto.appComponentId = data.appComponentId;
        taskCronDTO.dto.tableComponentId = data.tableComponentId;

        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    @ApiOperation("查询")
    @PostMapping("/get")
    public ResultEntity<Object> getData(@RequestBody TaskScheduleDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(dto));
    }


}
