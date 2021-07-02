package com.fisk.task.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 发送任务
 * @author gy
 */
@FeignClient("task-center")
public interface PublishTaskClient {

    /**
     * 发送任务创建消息
     * @param data dto
     * @return 发送结果
     */
    @PostMapping("/publishTask/nifiFlow")
    ResultEntity<Object> publishBuildNifiFlowTask(@RequestBody BuildNifiFlowDTO data);

    /**
     * 元数据构建
     * @param data dto
     * @return 构建结果
     */
    @PostMapping("/publishTask/atlasBuild")
    ResultEntity<Object> publishBuildAtlasTask(@RequestBody BuildNifiFlowDTO data);
}
