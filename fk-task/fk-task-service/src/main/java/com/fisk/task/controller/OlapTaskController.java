package com.fisk.task.controller;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigTaskDTO;
import com.fisk.task.consumer.olap.BuildModelTaskListener;
import com.fisk.task.consumer.olap.BuildWideTableTaskListener;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.service.task.IBuildTaskService;
import com.fisk.task.service.nifi.IOlap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
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
    @PostMapping("/CreateModel")
    public ResultEntity<Object> publishBuildAtomicKpiTask(@RequestBody BusinessAreaGetDataDTO businessAreaGetDataDTO) {
        log.info("进入方法");
        ResultEntity<Object> resultEntity = new ResultEntity<Object>();
        resultEntity.code=0;
        resultEntity.msg="流程创建成功";
        buildModelTaskListener.msg(JSON.toJSONString(businessAreaGetDataDTO),null);
        return resultEntity;
        /*return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_CREATEMODEL_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW,
                businessAreaGetDataDTO);*/
    }

    /**
     * 创建宽表模型
     *
     * @param wideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO
     * @return
     */
    @PostMapping("/publishBuildWideTableTask")
    public ResultEntity<Object> publishBuildWideTableTask(@RequestBody WideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO) {
        log.info("进入方法");
        ResultEntity<Object> resultEntity = new ResultEntity<Object>();
        resultEntity.code=0;
        resultEntity.msg="流程创建成功";
        buildWideTableTaskListener.msg(JSON.toJSONString(wideTableFieldConfigTaskDTO),null);
        return resultEntity;
        /*return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_WIDE_TABLE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_OLAP_WIDE_TABLE_FLOW,
                wideTableFieldConfigTaskDTO);*/
    }

    /**
     * selectByName
     *
     * @param tableName tableName
     * @return
     */
    @PostMapping("/selectByName")
    public ResultEntity<Object> selectByName(@RequestParam("tableName") String tableName) {
        ResultEntity<Object> olapPOResultEntity = new ResultEntity<>();
        OlapPO olapPO = olap.selectByName(tableName);
        olapPOResultEntity.data = olapPO.id;
        olapPOResultEntity.code = 0;
        return olapPOResultEntity;
    }
}
