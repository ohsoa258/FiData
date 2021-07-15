package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.task.dto.atlas.*;
import com.fisk.task.enums.AtlasProcessEnum;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuildInstance;
import com.rabbitmq.client.Channel;
import fk.atlas.api.model.EntityProcess;
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
import java.util.Date;
import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 14:01
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW)
@Slf4j
public class BuildAtlasTableAndColumnTaskListener {
    @Resource
    IAtlasBuildInstance atlas;
    @Resource
    DataAccessClient dc;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.ATLASTABLECOLUMN_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("进去Atlas生成表和字段");
        log.info("dataInfo:" + dataInfo);
        AtlasEntityQueryDTO inpData = JSON.parseObject(dataInfo, AtlasEntityQueryDTO.class);
        ResultEntity<AtlasEntityDbTableColumnDTO> queryRes = dc.getAtlasBuildTableAndColumn(Long.parseLong(inpData.dbId), Long.parseLong(inpData.appId));
        log.info("queryRes:" + JSON.toJSONString(queryRes.data));
        log.info("queryRes:" + JSON.toJSONString(queryRes));
        AtlasEntityDbTableColumnDTO ae = JSON.parseObject(JSON.toJSONString(queryRes.data), AtlasEntityDbTableColumnDTO.class);
        AtlasWriteBackDataDTO awbd = new AtlasWriteBackDataDTO();
        awbd.tableId=ae.tableId;
        awbd.appId=inpData.appId;
        awbd.tableName="ods_"+ae.tableName;
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
        awbd.atlasTableId = resTb.data.toString();
        //endregion
        //region atlas创建表与DB的连接
        AtlasEntityProcessDTO aepd = new AtlasEntityProcessDTO();
        List<EntityProcess.entity> inputs_db = new ArrayList<>();
        List<EntityProcess.entity> outputs_tab = new ArrayList<>();
        EntityProcess.entity inputentity_db = new EntityProcess.entity();
        EntityProcess.entity ouputentity_tab = new EntityProcess.entity();
        inputentity_db.guid = ae.dbId;
        inputentity_db.typeName = AtlasProcessEnum.db.getName();
        inputs_db.add(inputentity_db);
        ouputentity_tab.guid = awbd.atlasTableId;
        ouputentity_tab.typeName = AtlasProcessEnum.table.getName();
        outputs_tab.add(ouputentity_tab);
        aepd.createUser = ae.createUser;
        aepd.inputs = inputs_db;
        aepd.outputs = outputs_tab;
        aepd.higherType = AtlasProcessEnum.higherDb.getName();
        aepd.processName = "db_process_table_" + ae.tableName;
        aepd.qualifiedName = "db_process_table_" + ae.tableName + "@atlas";
        aepd.createUser = ae.createUser;
        aepd.des = "atlas process db link to table" + ae.tableName;
        atlas.atlasBuildProcess(aepd);
        //endregion
        //region atlas创建字段
        EntityRdbmsColumn.entity_rdbms_column entity_rdbms_column = new EntityRdbmsColumn.entity_rdbms_column();
        EntityRdbmsColumn.attributes_rdbms_column attributes_rdbms_column = new EntityRdbmsColumn.attributes_rdbms_column();
        EntityRdbmsColumn.instance_rdbms_table_column instance_rdbms_tableentity = new EntityRdbmsColumn.instance_rdbms_table_column();
        instance_rdbms_tableentity.guid = resTb.data.toString();
        List<AtlasEntityColumnDTO> l_acd = new ArrayList<>();
        StringBuilder sqlStr = new StringBuilder();
        ae.columns.forEach((c) -> {
            sqlStr.append(c.columnName + ",");
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
            acd.columnId=c.columnId;
            l_acd.add(acd);
        });
        String nifiSelectSql = sqlStr.toString();
        log.info(nifiSelectSql);
        nifiSelectSql = nifiSelectSql.substring(0, nifiSelectSql.lastIndexOf(","));
        nifiSelectSql = "select " + nifiSelectSql + " from " + ae.tableName;
        awbd.dorisSelectSqlStr = nifiSelectSql;
        awbd.columnsKeys = l_acd;
        log.info(JSON.toJSONString(awbd));
        //endregion
        //region atlas创建字段与表的连接
        EntityProcess.entity_rdbms_process entity_rdbms_process_table = new EntityProcess.entity_rdbms_process();
        List<EntityProcess.attributes_rdbms_process> earps = new ArrayList<>();
        EntityProcess.attributes_rdbms_process attributes_rdbms_process = new EntityProcess.attributes_rdbms_process();
        EntityProcess.attributes_field_rdbms_process attributes_field_rdbms_process = new EntityProcess.attributes_field_rdbms_process();
        List<EntityProcess.entity> inputs = new ArrayList<>();
        List<EntityProcess.entity> outputs = new ArrayList<>();
        EntityProcess.entity inputentity = new EntityProcess.entity();
        inputentity.guid = awbd.atlasTableId;
        inputentity.typeName = "rdbms_table";
        inputs.add(inputentity);
        l_acd.forEach((o) -> {
            EntityProcess.entity ouputentity = new EntityProcess.entity();
            ouputentity.guid = o.guid;
            ouputentity.typeName = "rdbms_column";
            outputs.add(ouputentity);
        });
        attributes_field_rdbms_process.owner = ae.createUser;
        attributes_field_rdbms_process.ownerName = ae.createUser;
        attributes_field_rdbms_process.name = "table_process_column_" + ae.tableName;
        attributes_field_rdbms_process.qualifiedName = "table_process_column_" + ae.tableName + "@atlas";
        attributes_field_rdbms_process.contact_info = "";
        attributes_field_rdbms_process.description = "atlas process column link to table " + ae.tableName;
        attributes_field_rdbms_process.createTime = df.format(new Date());
        attributes_field_rdbms_process.updateTime = df.format(new Date());
        attributes_field_rdbms_process.comment = ae.tableName + " process column";
        attributes_field_rdbms_process.type = "table";
        attributes_field_rdbms_process.inputs = inputs;
        attributes_field_rdbms_process.outputs = outputs;
        attributes_rdbms_process.attributes = attributes_field_rdbms_process;
        earps.add(attributes_rdbms_process);
        entity_rdbms_process_table.entities = earps;
        log.info(JSON.toJSONString(entity_rdbms_process_table));
        BusinessResult result = atlas.atlasBuildProcess(entity_rdbms_process_table);
        log.info(JSON.toJSONString(result));
        //endregion
        //region 回写数据
        dc.addAtlasTableIdAndDorisSql(awbd);
        //endregion
    }
}
