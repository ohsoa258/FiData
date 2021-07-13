package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.atlas.AtlasEntityColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuildInstance;
import com.fisk.task.service.IDorisBuild;
import com.rabbitmq.client.Channel;
import fk.atlas.api.model.EntityRdbmsColumn;
import fk.atlas.api.model.EntityRdbmsTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 14:01
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW)
@Slf4j
public class BuildAtalsTableAndColumnTaskListener {
    @Resource
    IAtlasBuildInstance atlas;
    @Resource
    IDorisBuild doris;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.ATLASTABLECOLUMN_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        AtlasEntityDbTableColumnDTO ae = JSON.parseObject(dataInfo, AtlasEntityDbTableColumnDTO.class);
        AtlasWriteBackDataDTO awbd = new AtlasWriteBackDataDTO();
        //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //region atlas创建表
        EntityRdbmsTable.entity_rdbms_table entity_rdbms_table = new EntityRdbmsTable.entity_rdbms_table();
        EntityRdbmsTable.attributes_rdbms_table attributes_rdbms_table = new EntityRdbmsTable.attributes_rdbms_table();
        EntityRdbmsTable.attributes_field_rdbms_table attributes_field_rdbms_table = new EntityRdbmsTable.attributes_field_rdbms_table();
        EntityRdbmsTable.instance_rdbms_table instance_rdbms_table = new EntityRdbmsTable.instance_rdbms_table();
        instance_rdbms_table.guid = ae.dbId;
        attributes_field_rdbms_table.owner = ae.createUser;
        attributes_field_rdbms_table.ownerName = ae.createUser;
        attributes_field_rdbms_table.name = ae.tableName;
        attributes_field_rdbms_table.qualifiedName = "db_" + ae.tableName + "@atalas";
        attributes_field_rdbms_table.description = "db_" + ae.tableName + "@atalas";
        attributes_field_rdbms_table.db = instance_rdbms_table;
        attributes_rdbms_table.attributes = attributes_field_rdbms_table;
        entity_rdbms_table.entity = attributes_rdbms_table;
        BusinessResult resTb = atlas.atlasBuildTable(entity_rdbms_table);
        awbd.tableId = resTb.data.toString();
        //endregion
        //region atlas创建字段
        EntityRdbmsColumn.entity_rdbms_column entity_rdbms_column = new EntityRdbmsColumn.entity_rdbms_column();
        EntityRdbmsColumn.attributes_rdbms_column attributes_rdbms_column = new EntityRdbmsColumn.attributes_rdbms_column();
        EntityRdbmsColumn.instance_rdbms_table_column instance_rdbms_tableentity = new EntityRdbmsColumn.instance_rdbms_table_column();
        instance_rdbms_tableentity.guid = resTb.data.toString();
        List<AtlasEntityColumnDTO> l_acd = new ArrayList<>();
        StringBuilder sqlStr = new StringBuilder();
        ae.columns.forEach((c) -> {
            sqlStr.append(c.columnName+",");
            AtlasEntityColumnDTO acd = new AtlasEntityColumnDTO();
            acd.columnName = c.columnName;
            EntityRdbmsColumn.attributes_field_rdbms_column attributes_field_rdbms_column = new EntityRdbmsColumn.attributes_field_rdbms_column();
            attributes_field_rdbms_column.table = instance_rdbms_tableentity;
            attributes_field_rdbms_column.owner = ae.createUser;
            attributes_field_rdbms_column.ownerName = ae.createUser;
            attributes_field_rdbms_column.name = c.columnName;
            attributes_field_rdbms_column.qualifiedName = ae.tableName + "_" + c.columnName + "_sex@atalas";
            attributes_field_rdbms_column.data_type = c.dataType;
            attributes_field_rdbms_column.comment = ae.tableName + "_" + c.columnName;
            attributes_field_rdbms_column.description = c.columnName;
            attributes_rdbms_column.attributes = attributes_field_rdbms_column;
            entity_rdbms_column.entity = attributes_rdbms_column;
            BusinessResult resCol = atlas.atlasBuildTableColumn(entity_rdbms_column);
            acd.guid = resCol.data.toString();
        });
        String nifiSelectSql=sqlStr.toString();
        nifiSelectSql = nifiSelectSql.substring(0, nifiSelectSql.lastIndexOf(",")) + ")";
        nifiSelectSql="select " + nifiSelectSql + " from " + ae.tableName;

        //endregion
        //region Doris创建表
        /*StringBuilder sql = new StringBuilder();
        String tableName = ae.tableName;
        String stg_table = "stg_" + ae.tableName;
        String ods_table = "ods_" + ae.tableName;
        sql.append("CREATE TABLE tableName");
        sql.append("(");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder sqlAggregate = new StringBuilder("AGGREGATE KEY(");
        StringBuilder sqlSelectStrBuild = new StringBuilder();
        StringBuilder sqlDistributed = new StringBuilder("DISTRIBUTED BY HASH(");
        ae.columns.forEach((l) -> {
            //是否是主键
            if (l.isKey.equals("1")) {
                sqlDistributed.append(l.columnName);
            }
            sqlFileds.append(l.columnName + " " + l.dataType + " comment " + "'" + l.comment + "' ,");
            sqlAggregate.append(l.columnName + ",");
            sqlSelectStrBuild.append(l.columnName + ",");
        });
        sqlDistributed.append(") BUCKETS 10");
        String aggregateStr = sqlAggregate.toString();
        aggregateStr = aggregateStr.substring(0, aggregateStr.lastIndexOf(",")) + ")";
        String selectStr = sqlSelectStrBuild.toString();
        selectStr = selectStr.substring(0, selectStr.lastIndexOf(",")) + ")";
        String filedStr = sqlFileds.toString();
        sql.append(filedStr.substring(0, filedStr.lastIndexOf(",")));
        String sqlSelectStr = "select " + selectStr + " from " + ae.tableName;
        sql.append(")");
        sql.append(aggregateStr);
        sql.append(sqlDistributed.toString());
        sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
        String stg_sql = sql.toString().replace("tableName", stg_table);
        String ods_sql = sql.toString().replace("tableName", ods_table);
        BusinessResult sqlResult_stg = doris.dorisBuildTable(stg_sql);
        BusinessResult sqlResult_ods = doris.dorisBuildTable(ods_sql);
        BuildNifiFlowDTO bb = new BuildNifiFlowDTO();
        bb.appId = 123L;
        bb.userId = 37L;*/
        //endregion
    }
}
