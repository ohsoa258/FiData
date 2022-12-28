package com.fisk.task.listener.metadata.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.task.dto.task.BuildMetaDataDTO;
import com.fisk.task.listener.metadata.IMetaDataListener;
import com.fisk.task.server.TaskThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class BuildMetaDataListener implements IMetaDataListener {

    @Resource
    DataManageClient dataManageClient;

    @Override
    public ResultEnum metaData(String dataInfo, Acknowledgment ack) {
        try {
            log.info("元数据实时同步数据:{}", dataInfo);
            TaskThreadPool.TASK_POOL.submit(() -> {
                try {
                    log.info("*****开始推送元数据实时同步数据*****");
                    BuildMetaDataDTO data = JSONObject.parseObject(dataInfo, BuildMetaDataDTO.class);
                    dataManageClient.consumeMetaData(data.data);
                } catch (Exception e) {
                    log.error("【推送元数据实时数据失败】,{}", e);
                }
            });
        } catch (Exception e) {
            log.error("元数据实时同步失败 ex:", e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
        return ResultEnum.SUCCESS;
    }

}
