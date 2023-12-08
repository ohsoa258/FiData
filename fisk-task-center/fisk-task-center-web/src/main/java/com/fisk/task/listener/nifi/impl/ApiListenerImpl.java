package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.listener.nifi.IApiListener;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Objects;

/**
 * @author lsj
 */
@Service
@Slf4j
public class ApiListenerImpl implements IApiListener {

    @Resource
    DataAccessClient client;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;

    @Override
    public ResultEntity<Object> apiToStg(String data) {
        log.info("api-Java代码同步参数:{}", data);
        ResultEntity<Object> apiToStgResult = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            KafkaReceiveDTO kafkaReceive = JSON.parseObject(data, KafkaReceiveDTO.class);
            //获取topic
            String topic = kafkaReceive.topic;
            //获取大批次号
            String fidata_batch_code = kafkaReceive.fidata_batch_code;
            log.info("大批次号：{}", fidata_batch_code);

            String[] topicParameter = topic.split("\\.");
            //应用id
            String appId = "";
            //表id
            String tableId = "";
            //apiid
            long apiId;
            if (Objects.equals(topicParameter.length, 6)) {
                appId = topicParameter[4];
                tableId = topicParameter[5];
            } else if (Objects.equals(topicParameter.length, 7)) {
                appId = topicParameter[5];
                tableId = topicParameter[6];
            }

            //todo:通过物理表id,获取apiid
            ResultEntity<TableAccessDTO> result = client.getTableAccess(Integer.parseInt(tableId));
            if (ResultEnum.SUCCESS.getCode() != result.getCode()) {
                log.error("api-Java代码同步获取apiId失败 - 未获取到apiId");
                return ResultEntityBuild.build(ResultEnum.APICONFIG_ISNULL);
            }
            TableAccessDTO tableAccessDTO = result.getData();
            apiId = tableAccessDTO.getApiId();

            com.fisk.dataaccess.dto.api.ApiImportDataDTO apiImportDataDTO = new com.fisk.dataaccess.dto.api.ApiImportDataDTO();
            apiImportDataDTO.setAppId(Long.parseLong(appId));
            apiImportDataDTO.setApiId(apiId);
            apiImportDataDTO.setBatchCode(fidata_batch_code);

            //远程调用api同步数据的方法 将数据同步进stg
            apiToStgResult = client.importDataV2(apiImportDataDTO);

        } catch (Exception e) {
            log.error("api-Java代码同步报错" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEntityBuild.build(ResultEnum.API_NIFI_SYNC_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(pstmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return apiToStgResult;
    }

}
