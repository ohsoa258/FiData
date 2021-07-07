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

    /** 在Doris中生成stg&ods数据表
     * @param data
     * @param buildType
     * @return
     */
    @PostMapping("/dorisBuild")
    public ResultEntity<Object> publishBuildDorisTask(@RequestBody TableInfoDTO data,String buildType) {
        return service.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_DORIS_FLOW,
                data);
    }
    /** 在Atlas中生成元数据的血缘关系
     * @param data
     * @param buildType
     * @return
     */
    @PostMapping("/atlasBuild")
    public ResultEntity<Object> publishBuildAtlasTask(@RequestBody TableInfoDTO data,String buildType) {
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_ATLAS_FLOW,
                data);
    }
}
