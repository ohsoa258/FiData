package com.fisk.task.controller;

import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.task.dto.olap.BuildCreateModelTaskDto;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.service.IOlap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *
 * @author JinXingWang
 */
@Slf4j
@RestController
@RequestMapping("/olapTask")
public class OlapTaskController {

    @Resource
    IBuildTaskService service;
    @Resource
    IOlap olap;
    /**
     * 创建模型
     * @param buildCreateModelTaskDto
     * @return
     */
    @PostMapping("/CreateModel")
    public ResultEntity<Object> publishBuildAtomicKpiTask(@RequestBody BuildCreateModelTaskDto buildCreateModelTaskDto){
        return service.publishTask(TaskTypeEnum.BUILD_CREATEMODEL_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW,
                buildCreateModelTaskDto);
    }
    @PostMapping("/selectByName")
    public ResultEntity<Object> selectByName(@RequestParam("tableName")String tableName){
        ResultEntity<Object> olapPOResultEntity = new ResultEntity<>();
        OlapPO olapPO = olap.selectByName(tableName);
        olapPOResultEntity.data=olapPO.id;
        olapPOResultEntity.code=0;
        return olapPOResultEntity;
    }
}
