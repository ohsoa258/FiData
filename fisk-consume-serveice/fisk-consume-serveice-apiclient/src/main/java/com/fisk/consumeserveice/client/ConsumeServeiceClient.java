package com.fisk.consumeserveice.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataApplicationDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceEmailDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePublishStatusDTO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author JianWenYang
 */
@FeignClient("dataservice-service")
public interface ConsumeServeiceClient {

    /**
     * 根据管道id获取表服务集合
     *
     * @param id
     * @return
     */
    @ApiOperation("根据管道id获取表服务集合")
    @GetMapping("/tableService/getTableListByPipelineId/{id}")
    ResultEntity<List<BuildTableServiceDTO>> getTableListByPipelineId(@PathVariable("id") Integer id);

    /**
     * 根据管道id获取数据分发api集合
     *
     * @param id
     * @return
     */
    @ApiOperation("根据管道id获取数据分发api集合")
    @GetMapping("/tableApiService/getTableApiListByPipelineId/{id}")
    ResultEntity<List<BuildTableApiServiceDTO>> getTableApiListByPipelineId(@PathVariable("id") Integer id);

    /**
     * 修改表服务发布状态
     *
     * @param dto
     */
    @ApiOperation("修改表服务发布状态")
    @PutMapping("/tableService/updateTableServiceStatus")
    void updateTableServiceStatus(@RequestBody TableServicePublishStatusDTO dto);

    /**
     * 修改数据分发服务api发布状态
     *
     * @param dto
     */
    @ApiOperation("修改数据分发服务api发布状态")
    @PutMapping("/tableApiService/updateTableApiStatus")
    void updateTableApiStatus(@RequestBody TableServicePublishStatusDTO dto);

    /**
     * 根据表服务id构建发布数据
     *
     * @param id
     * @return
     */
    @ApiOperation("根据表服务id构建发布数据")
    @GetMapping("/tableService/getBuildTableServiceById/{id}")
    ResultEntity<BuildTableServiceDTO> getBuildTableServiceById(@PathVariable("id") long id);

    /**
     * 获取API服务的所有应用
     * @return
     */
    @GetMapping("/apiTableViewService/getApiTableViewService")
    ResultEntity<List<AppBusinessInfoDTO>> getApiTableViewService();

    /**
     * 获取API服务&Table服务&View服务的所有应用
     * @return
     */
    @GetMapping("/appRegister/getApiService")
    ResultEntity<List<AppBusinessInfoDTO>> getApiService();

    /**
     * 获取Table服务的所有应用
     * @return
     */
    @GetMapping("/tableService/getTableService")
    ResultEntity<List<AppBusinessInfoDTO>> getTableService();

    /**
     * 获取View服务的所有应用
     * @return
     */
    @GetMapping("/dataAnalysisView/getViewService")
    ResultEntity<List<AppBusinessInfoDTO>> getViewService();

    /**
     * 元数据同步API服务应用信息
     * @return
     */
    @GetMapping("/apiTableViewService/synchronizationAPIAppRegistration")
    ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAPIAppRegistration();


    /**
     * 发送表服务应用告警通知
     * @param dto
     * @return
     */
    @PostMapping("/tableService/tableServiceSendEmails")
    ResultEntity<Object> tableServiceSendEmails(@RequestBody TableServiceEmailDTO dto);

    /**
     * 获取API服务应用信息元数据
     * @return
     */
    @GetMapping("/appRegister/getApiMetaData")
    ResultEntity<List<MetaDataEntityDTO>> getApiMetaData();

    /**
     * 获取视图服务据应用信息元数
     * @return
     */
    @GetMapping("/dataAnalysisView/getViewServiceMetaData")
    ResultEntity<List<MetaDataEntityDTO>> getViewServiceMetaData();

    /**
     * 获取数据库同步服务应用信息元数据
     * @return
     */
    @GetMapping("/tableService/getTableSyncMetaData")
    ResultEntity<List<MetaDataEntityDTO>> getTableSyncMetaData();
}
