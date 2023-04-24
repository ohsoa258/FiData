package com.fisk.task.controller;

import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TaskTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigTaskDTO;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.dto.task.UnifiedControlDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.listener.olap.BuildModelTaskListener;
import com.fisk.task.listener.olap.BuildWideTableTaskListener;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.service.task.IBuildTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JinXingWang
 */
@Api(tags = {SwaggerConfig.OlapTask})
@Slf4j
@RestController
@RequestMapping("/olapTask")
public class OlapTaskController {

    @Resource
    IBuildTaskService service;
    @Resource
    IOlap olap;
    @Resource
    IBuildKfkTaskService iBuildKfkTaskService;
    @Resource
    BuildModelTaskListener buildModelTaskListener;
    @Resource
    BuildWideTableTaskListener buildWideTableTaskListener;

    /**
     * 创建模型
     *
     * @param businessAreaGetDataDTO
     * @return
     */
    @ApiOperation("创建模型")
    @PostMapping("/CreateModel")
    public ResultEntity<Object> publishBuildAtomicKpiTask(@RequestBody BusinessAreaGetDataDTO businessAreaGetDataDTO) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_CREATEMODEL_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.OlapTopicConstants.BUILD_OLAP_CREATEMODEL_FLOW,
                businessAreaGetDataDTO);
    }

    /**
     * 创建宽表模型
     *
     * @param wideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO
     * @return
     */
    @ApiOperation("创建宽表模型")
    @PostMapping("/publishBuildWideTableTask")
    public ResultEntity<Object> publishBuildWideTableTask(@RequestBody WideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO) {

        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_WIDE_TABLE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.OlapTopicConstants.BUILD_OLAP_WIDE_TABLE_FLOW,
                wideTableFieldConfigTaskDTO);
    }

    /**
     * 统一调度
     *
     * @param unifiedControlDTO unifiedControlDTO
     * @return
     */
    @ApiOperation("统一调度")
    @PostMapping("/publishBuildunifiedControlTask")
    public ResultEntity<Object> publishBuildunifiedControlTask(@RequestBody UnifiedControlDTO unifiedControlDTO) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_TASK_BUILD_NIFI_DISPATCH_FLOW.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DispatchTopicConstants.BUILD_TASK_BUILD_NIFI_DISPATCH_FLOW,
                unifiedControlDTO);
    }

    /**
     * selectByName
     *
     * @param tableName tableName
     * @return
     */
    @ApiOperation("通过名字查找")
    @PostMapping("/selectByName")
    public ResultEntity<Object> selectByName(@RequestParam("tableName") String tableName) {
        ResultEntity<Object> olapPOResultEntity = new ResultEntity<>();
        OlapPO olapPO = olap.selectByName(tableName);
        olapPOResultEntity.data = olapPO.id;
        olapPOResultEntity.code = 0;
        return olapPOResultEntity;
    }
}
