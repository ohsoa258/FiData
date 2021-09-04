package com.fisk.task.controller;

import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.task.service.IBuildTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *
 * @author JinXingWang
 */
@Slf4j
@RestController
@RequestMapping("/olapTask")
public class OLAPTaskController {

    @Resource
    IBuildTaskService service;
    /**
     * 创建模型
     * @param dimensionAttributeAddDTO
     * @return
     */
    @PostMapping("/atlasDorisTable")
    public ResultEntity<Object> publishBuildAtomicKpiTask(@RequestBody DimensionAttributeAddDTO dimensionAttributeAddDTO){
        return service.publishTask(TaskTypeEnum.BUILD_CREATEMODEL_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW,
                dimensionAttributeAddDTO);
    }
}
