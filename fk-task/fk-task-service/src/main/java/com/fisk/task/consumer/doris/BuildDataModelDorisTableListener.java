package com.fisk.task.consumer.doris;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IDorisBuild;
import com.fisk.task.service.IPostgreBuild;
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
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE)
@Slf4j
public class BuildDataModelDorisTableListener {
    @Resource
    DataModelClient dc;
    @Resource
    IDorisBuild doris;
    @Resource
    IPostgreBuild iPostgreBuild;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAMODEL_DORIS_TABLE_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        DimensionAttributeAddDTO inpData = JSON.parseObject(dataInfo, DimensionAttributeAddDTO.class);
        ResultEntity<ModelMetaDataDTO> dimensionAttributeList =new ResultEntity<>();
        if(inpData.createType==0){
            dimensionAttributeList=dc.getDimensionEntity(inpData.dimensionId);
        }else {
            dimensionAttributeList=dc.getFactEntity(inpData.dimensionId);
        }
        ModelMetaDataDTO modelMetaDataDTO = JSON.parseObject(JSON.toJSONString(dimensionAttributeList.data), ModelMetaDataDTO.class);
        boolean pgdbTable = createPgdbTable(modelMetaDataDTO);
        log.info("pg数据库创表结果为" + pgdbTable);
        List<ModelAttributeMetaDataDTO> dto = modelMetaDataDTO.dto;
        StringBuilder sql = new StringBuilder();
        String stg_table = "stg_" + modelMetaDataDTO.tableName;
        String ods_table = "ods_" + modelMetaDataDTO.tableName;
        String stg_sql = "";
        String ods_sql = "";
        sql.append("CREATE TABLE tableName (");
        StringBuilder sqlFileds = new StringBuilder();
        List<ModelAttributeMetaDataDTO> dto2 = modelMetaDataDTO.dto;
        List<Integer> collect = dto2.stream().map(e -> e.attributeType).collect(Collectors.toList());
        //先判断是不是维度表,再根据是否是业务主键,判断建表模型
        if (inpData.createType == 1) {
            sqlFileds.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' , doris_custom_data_flag varchar(2) DEFAULT \"1\" comment '系统字段，默认分桶' ");
            dto.forEach((l) -> {
                sqlFileds.append("," + l.fieldEnName + " " + l.fieldType + " comment " + " ''");
            });
            sqlFileds.append(")");
            sql.append(sqlFileds);
            StringBuilder sqlSelectStrBuild = new StringBuilder("DUPLICATE KEY(fk_doris_increment_code  , doris_custom_data_flag ) DISTRIBUTED BY HASH(fk_doris_increment_code) BUCKETS 10 PROPERTIES ( \"replication_num\" = \"1\" )");
            sql.append(sqlSelectStrBuild);
        } else {
            StringBuilder sqlDistributed = new StringBuilder("DISTRIBUTED BY HASH(");
            StringBuilder sqlSelectStrBuild = new StringBuilder("UNIQUE KEY(");
            dto.forEach((l) -> {
                sqlFileds.append(l.fieldEnName + " " + l.fieldType + " comment " + " '',");
                //是否是业务主键
                if (collect.contains(0)) {
                    sqlDistributed.append(l.fieldEnName + ",");
                    sqlSelectStrBuild.append(l.fieldEnName + ",");
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
    private boolean createPgdbTable(ModelMetaDataDTO modelMetaDataDTO){
        //创建pgdb表
        //1.拼接sql
        StringBuilder sql = new StringBuilder();
        String stg_table = "stg_" + modelMetaDataDTO.tableName;
        String ods_table = "ods_" + modelMetaDataDTO.tableName;
        String stg_sql = "";
        String ods_sql = "";
        sql.append("CREATE TABLE tableName (");
        StringBuilder sqlFileds = new StringBuilder();
        List<ModelAttributeMetaDataDTO> dto = modelMetaDataDTO.dto;
        dto.forEach((l) -> {
            sqlFileds.append( l.fieldEnName + " " + l.fieldType + " ,");
        });
        sqlFileds.delete(sqlFileds.length()-1,sqlFileds.length());
        sqlFileds.append(")");
        sql.append(sqlFileds);
        stg_sql = sql.toString().replace("tableName", stg_table);
        ods_sql = sql.toString().replace("tableName", ods_table);
        //2.连接jdbc执行sql
        BusinessResult datamodel = iPostgreBuild.postgreBuildTable(stg_sql, BusinessTypeEnum.DATAMODEL);
        BusinessResult datamodel1 = iPostgreBuild.postgreBuildTable(ods_sql, BusinessTypeEnum.DATAMODEL);
        log.info("【PGSTG】" + stg_sql);
        log.info("【PGODS】" + ods_sql);
        if(datamodel.success==true&&datamodel1.success==true){
            return true;
        }else {
            return false;
        }
    }
}
