package com.fisk.task.consumer.doris;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.client.DimensionClient;
import com.fisk.datamodel.dto.dimension.DimensionMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeMetaDataDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IDorisBuild;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: cfk
 * CreateTime: 2021/08/03 15:05
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DORIS_TABLE)
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DORIS_FLOW)
@Slf4j
public class BuildDataModelDorisTableListener {
    @Resource
    DimensionClient dc;
    @Resource
    IDorisBuild doris;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DORIS_TABLE_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        DimensionAttributeAddDTO inpData = JSON.parseObject(dataInfo, DimensionAttributeAddDTO.class);
        ResultEntity<DimensionMetaDataDTO> dimensionAttributeList = dc.getDimensionEntity(inpData.dimensionId);
        DimensionMetaDataDTO dimensionMetaDataDTO = JSON.parseObject(JSON.toJSONString(dimensionAttributeList.data), DimensionMetaDataDTO.class);
        List<DimensionAttributeMetaDataDTO> dto = dimensionMetaDataDTO.dto;
        StringBuilder sql = new StringBuilder();
        String stg_table = "stg_" + dimensionMetaDataDTO.dimensionTabName;
        String ods_table = "ods_" + dimensionMetaDataDTO.dimensionTabName;
        String stg_sql = "";
        String ods_sql = "";
        sql.append("CREATE TABLE tableName (");
        StringBuilder sqlFileds = new StringBuilder();
        List<DimensionAttributeMetaDataDTO> dto2 = dimensionMetaDataDTO.dto;
        List<Integer> collect = dto2.stream().map(e -> e.attributeType).collect(Collectors.toList());
        //先判断是不是维度表,再根据是否是业务主键,判断建表模型
        if (inpData.createType == 1) {
            sqlFileds.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' , doris_custom_data_flag varchar(2) DEFAULT \"1\" comment '系统字段，默认分桶' ");
            dto.forEach((l) -> {
                sqlFileds.append("," + l.dimensionFieldEnName + " " + l.dimensionFieldType + " comment " + " ''");
            });
            sqlFileds.append(")");
            sql.append(sqlFileds);
            StringBuilder sqlSelectStrBuild = new StringBuilder("DUPLICATE KEY(fk_doris_increment_code  , doris_custom_data_flag ) DISTRIBUTED BY HASH(fk_doris_increment_code) BUCKETS 10 PROPERTIES ( \"replication_num\" = \"1\" )");
            sql.append(sqlSelectStrBuild);
        } else {
            StringBuilder sqlDistributed = new StringBuilder("DISTRIBUTED BY HASH(");
            StringBuilder sqlSelectStrBuild = new StringBuilder("UNIQUE KEY(");
            dto.forEach((l) -> {
                sqlFileds.append(l.dimensionFieldEnName + " " + l.dimensionFieldType + " comment " + " '',");
                //是否是业务主键
                if (collect.contains(0)) {
                    sqlDistributed.append(l.dimensionFieldEnName + ",");
                    sqlSelectStrBuild.append(l.dimensionFieldEnName + ",");
                }
            });
            sqlFileds.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' ,");
            String sqlSelectStrBuildStr = sqlSelectStrBuild.toString();
            sqlSelectStrBuildStr = sqlSelectStrBuildStr.substring(0, sqlSelectStrBuildStr.lastIndexOf(",")) + ")";
            String sqlDistributedStr = sqlDistributed.toString();
            sqlDistributedStr = sqlDistributedStr.substring(0, sqlDistributed.lastIndexOf(",")) + ") BUCKETS 10";
            sql.append(sqlFileds.append(" doris_custom_data_flag varchar(2) DEFAULT \"1\" comment '系统字段，默认分桶'").toString());
            sql.append(")");
            sql.append(sqlSelectStrBuildStr);
            sql.append(sqlDistributedStr);
            sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");

        }
        stg_sql = sql.toString().replace("tableName", stg_table);
        ods_sql = sql.toString().replace("tableName", ods_table);
        doris.dorisBuildTable(stg_sql);
        doris.dorisBuildTable(ods_sql);
        log.info("【STG】" + stg_sql);
        log.info("【ODS】" + ods_sql);
    }
}
