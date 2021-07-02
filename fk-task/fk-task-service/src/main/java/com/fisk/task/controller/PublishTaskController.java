package com.fisk.task.controller;

import com.fisk.common.constants.MQConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.atlas.TableInfoDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.service.IBuildTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author gy
 */
@RestController
@RequestMapping("/publishTask")
@Slf4j
public class PublishTaskController {

    @Resource
    IBuildTaskService service;

    @PostMapping("/nifiFlow")
    public ResultEntity<Object> publishBuildNifiFlowTask(@RequestBody BuildNifiFlowDTO data) {
        return service.publishTask(TaskTypeEnum.BUILD_NIFI_FLOW.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_NIFI_FLOW,
                data);
    }
    @PostMapping("/atlasBuild")
    public ResultEntity<Object> publishBuildAtlasTask(@RequestBody TableInfoDTO data) {
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_ATLAS_FLOW,
                data);
    }
}
