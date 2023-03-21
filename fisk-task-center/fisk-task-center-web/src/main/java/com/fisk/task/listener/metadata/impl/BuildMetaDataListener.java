package com.fisk.task.listener.metadata.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.task.dto.metadatafield.MetaDataFieldDTO;
import com.fisk.task.listener.metadata.IMetaDataListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            /*log.info("元数据实时同步数据:{}", dataInfo);
            TaskThreadPool.TASK_POOL.submit(() -> {
                try {
                    log.info("*****开始推送元数据实时同步数据*****");
                    BuildMetaDataDTO data = JSONObject.parseObject(dataInfo, BuildMetaDataDTO.class);
                    dataManageClient.consumeMetaData(data.data);
                } catch (Exception e) {
                    log.error("【推送元数据实时数据失败】,{}", e);
                }
            });*/
        } catch (Exception e) {
            log.error("元数据实时同步失败 ex:", e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum fieldDelete(String dataInfo, Acknowledgment ack) {
        try {
            MetaDataFieldDTO metaDataFieldDTO = JSON.parseObject(dataInfo, MetaDataFieldDTO.class);
            List<MetadataEntityDTO> resultEntity = dataManageClient.queryMetadaFildes(metaDataFieldDTO.getTableId(), metaDataFieldDTO.getFieldId());
            if(resultEntity==null){
                log.debug("数据不存在");
            }
            List<Integer> ids =resultEntity.stream().map(m->(int)m.getId()).collect(Collectors.toList());
            dataManageClient.fieldDelete(ids);
            MetaDataEntityOperationLogDTO logDTO = new MetaDataEntityOperationLogDTO();
            //拿到该字段表的ID 不用记录stg的
            logDTO.setMetadataEntityId(String.valueOf(resultEntity.stream().findFirst().get().parentId));
            logDTO.setCreateUser(String.valueOf(metaDataFieldDTO.getUserId()));
            logDTO.setCreateTime(LocalDateTime.now());
            logDTO.setBeforeChange(resultEntity.stream().findFirst().get().getName());
            logDTO.setAfterChange("");
            logDTO.setOperationType("删除");
            dataManageClient.saveLog(logDTO);
        } catch (Exception e) {
            log.error("元数据字段删除失败 ex：+"+e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
        return ResultEnum.SUCCESS;
    }

}
