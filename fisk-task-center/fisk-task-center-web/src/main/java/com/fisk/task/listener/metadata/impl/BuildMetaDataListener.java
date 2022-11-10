package com.fisk.task.listener.metadata.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.task.dto.task.BuildMetaDataDTO;
import com.fisk.task.listener.metadata.IMetaDataListener;
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
            log.info("元数据实时同步数据:", dataInfo);
            BuildMetaDataDTO data = JSONObject.parseObject(dataInfo, BuildMetaDataDTO.class);
            ResultEntity<Object> result = dataManageClient.consumeMetaData(data.data);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
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
