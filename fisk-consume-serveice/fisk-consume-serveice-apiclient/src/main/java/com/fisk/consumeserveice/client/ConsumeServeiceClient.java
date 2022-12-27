package com.fisk.consumeserveice.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

}
