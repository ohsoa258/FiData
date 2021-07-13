package com.fisk.task.controller;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
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
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_NIFI_FLOW,
                data);
    }

    /**
     * 在Doris中生成stg&ods数据表
     *
     * @param data
     * @return
     */
    @PostMapping("/dorisBuild")
    public ResultEntity<Object> publishBuildDorisTask(@RequestBody TableInfoDTO data) {
        return service.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DORIS_FLOW,
                data);
    }

    /**
     * 在Atlas中生成实例与数据库的血缘关系
     *
     * @param ArDto
     * @return
     */
    @PostMapping("/atlasBuildInstance")
    public ResultEntity<Object> publishBuildAtlasInstanceTask(@RequestBody AtlasEntityDTO ArDto) {
        log.debug(JSON.toJSONString(ArDto));
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ATLAS_INSTANCE_FLOW,
                ArDto);
    }

    /**
     * 在Atlas中生成数据库、表、字段的血缘关系
     *
     * @param ArDto
     * @return
     */
    @PostMapping("/atlasBuildTableAndColumn")
    public ResultEntity<Object> publishBuildAtlasTableTask(@RequestBody AtlasEntityDTO ArDto) {
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW,
                ArDto);
    }

    @PostMapping("/atlasEntityDelete")
    public ResultEntity<Object> publishBuildAtlasEntityDeleteTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_ENTITYDELETE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ATLAS_ENTITYDELETE_FLOW,
                entityId);
    }

}
