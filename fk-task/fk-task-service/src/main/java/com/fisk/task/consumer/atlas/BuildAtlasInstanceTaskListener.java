package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuildInstance;
import com.rabbitmq.client.Channel;
import fk.atlas.api.model.EntityProcess;
import fk.atlas.api.model.EntityRdbmsDB;
import fk.atlas.api.model.EnttityRdbmsInstance;
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
 * CreateTime: 2021/7/7 15:57
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_ATLAS_INSTANCE_FLOW)
@Slf4j
public class BuildAtlasInstanceTaskListener {

    @Resource
    IAtlasBuildInstance atlas;
    @Resource
    DataAccessClient dc;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.ATLASINSTANCE_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("data:"+dataInfo);
        AtlasEntityQueryDTO inpData=JSON.parseObject(dataInfo, AtlasEntityQueryDTO.class);
        ResultEntity<AtlasEntityDTO> queryRes= dc.getAtlasEntity(Long.parseLong(inpData.appId));
        log.info("query data :"+JSON.toJSONString(queryRes));
        AtlasWriteBackDataDTO awbd = new AtlasWriteBackDataDTO();
        AtlasEntityDTO ae = JSON.parseObject(JSON.toJSONString(queryRes.data), AtlasEntityDTO.class);
        //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //region  创建实例
        EnttityRdbmsInstance.attributes_rdbms_instance ari = new EnttityRdbmsInstance.attributes_rdbms_instance();
        EnttityRdbmsInstance.attributes_field_rdbms_instance arif = new EnttityRdbmsInstance.attributes_field_rdbms_instance();
        arif.qualifiedName = ae.appName + "@atlas_instance";
        arif.name = ae.appName;
        arif.rdbms_type = ae.driveType;
        arif.platform = "windows";
        arif.hostname = ae.host;
        arif.port = ae.port;
        arif.protocol = ae.driveType + " protocal";
        arif.contact_info = "your contact info";
        arif.description = ae.appDes;
        arif.owner = ae.createUser;
        arif.ownerName = ae.createUser;
        ari.attributes = arif;
        EnttityRdbmsInstance.entity_rdbms_instance eri = new EnttityRdbmsInstance.entity_rdbms_instance();
        eri.entity = ari;
        BusinessResult insRes = atlas.atlasBuildInstance(eri);
        awbd.appId = insRes.data.toString();
        //endregion
        //region 创建DB
        EntityRdbmsDB.entity_rdbms_db rdbms_db = new EntityRdbmsDB.entity_rdbms_db();
        EntityRdbmsDB.attributes_rdbms_db attributes_rdbms_db = new EntityRdbmsDB.attributes_rdbms_db();
        EntityRdbmsDB.attributes_field_rdbms_db attributes_field_rdbms_db = new EntityRdbmsDB.attributes_field_rdbms_db();
        EntityRdbmsDB.instance_rdbms_db instance_rdbms_db = new EntityRdbmsDB.instance_rdbms_db();
        instance_rdbms_db.guid = insRes.data.toString();
        instance_rdbms_db.entityStatus = "ACTIVE";
        attributes_field_rdbms_db.owner = ae.createUser;
        attributes_field_rdbms_db.ownerName = ae.createUser;
        attributes_field_rdbms_db.name = ae.dbName;
        attributes_field_rdbms_db.qualifiedName = ae.dbName + "@atlas_db";
        attributes_field_rdbms_db.displayText = ae.dbName;
        attributes_field_rdbms_db.description = "";
        attributes_field_rdbms_db.instance = instance_rdbms_db;
        attributes_rdbms_db.attributes = attributes_field_rdbms_db;
        rdbms_db.entity = attributes_rdbms_db;
        BusinessResult dbRes = atlas.atlasBuildDb(rdbms_db);
        awbd.dbId = dbRes.data.toString();
        //endregion
        //region 创建实例与数据库的连接
        EntityProcess.entity_rdbms_process entity_rdbms_process = new EntityProcess.entity_rdbms_process();
        List<EntityProcess.attributes_rdbms_process> earps = new ArrayList<>();
        EntityProcess.attributes_rdbms_process attributes_rdbms_process = new EntityProcess.attributes_rdbms_process();
        EntityProcess.attributes_field_rdbms_process attributes_field_rdbms_process = new EntityProcess.attributes_field_rdbms_process();
        List<EntityProcess.entity> inputs = new ArrayList<>();
        List<EntityProcess.entity> outputs = new ArrayList<>();
        EntityProcess.entity inputentity = new EntityProcess.entity();
        EntityProcess.entity ouputentity = new EntityProcess.entity();
        inputentity.guid = insRes.data.toString();
        inputentity.typeName = "rdbms_instance";
        inputs.add(inputentity);
        ouputentity.guid = dbRes.data.toString();
        ouputentity.typeName = "rdbms_db";
        outputs.add(ouputentity);
        attributes_field_rdbms_process.owner = ae.createUser;
        attributes_field_rdbms_process.ownerName = ae.createUser;
        attributes_field_rdbms_process.name = ae.appName + "_instance_process_db_" + ae.dbName;
        attributes_field_rdbms_process.qualifiedName = ae.appName + "_instance_process_db_" + ae.dbName + "@atlas";
        attributes_field_rdbms_process.contact_info = "";
        attributes_field_rdbms_process.description = ae.appName + " process " + ae.dbName;
        attributes_field_rdbms_process.createTime = df.format(new Date());
        attributes_field_rdbms_process.updateTime = df.format(new Date());
        attributes_field_rdbms_process.comment = "";
        attributes_field_rdbms_process.type = "instance";
        attributes_field_rdbms_process.inputs = inputs;
        attributes_field_rdbms_process.outputs = outputs;
        attributes_rdbms_process.attributes = attributes_field_rdbms_process;
        earps.add(attributes_rdbms_process);
        entity_rdbms_process.entities = earps;
        atlas.atlasBuildProcess(entity_rdbms_process);
        //endregion
        //region 返回instance id & DB id
        //。。。。。。
        //endregion
    }
}
