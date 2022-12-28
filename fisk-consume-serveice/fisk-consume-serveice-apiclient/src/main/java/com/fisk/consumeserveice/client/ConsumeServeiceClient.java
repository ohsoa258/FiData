package com.fisk.consumeserveice.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.dataservice.dto.tableservice.TableServicePublishStatusDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
     * 修改表服务发布状态
     *
     * @param dto
     */
    @ApiOperation("修改表服务发布状态")
    @PutMapping("/tableService/updateTableServiceStatus")
    void updateTableServiceStatus(@RequestBody TableServicePublishStatusDTO dto);

    /**
     * 根据表服务id构建发布数据
     *
     * @param id
     * @return
     */
    @ApiOperation("根据表服务id构建发布数据")
    @GetMapping("/tableService/getBuildTableServiceById/{id}")
    ResultEntity<List<BuildTableServiceDTO>> getBuildTableServiceById(@PathVariable("id") Integer id);

}
