package task;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.doris.TableColumnInfoDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.service.IAtlasBuildInstance;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.service.IDorisBuild;
import fk.atlas.api.model.EntityProcess;
import fk.atlas.api.model.EntityRdbmsDB;
import fk.atlas.api.model.EnttityRdbmsInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author:yhxu
 * CreateTime: 2021/7/1 10:19
 * Description:
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FkTaskApplication.class)
@RunWith(SpringRunner.class)
public class AtlasTest {
    @Resource
    IDorisBuild doris;
    @Resource
    IAtlasBuildInstance atlas;
    IBuildTaskService service;
    @Test
    public void testdoriscreatetable() {
        TableInfoDTO tab = new TableInfoDTO();
        tab.tableName = "denny_table";
        String stg_table="stg_"+tab.tableName;
        String ods_table="ods_"+tab.tableName;
        TableColumnInfoDTO tl = new TableColumnInfoDTO();
        TableColumnInfoDTO tl2 = new TableColumnInfoDTO();
        TableColumnInfoDTO tl3 = new TableColumnInfoDTO();
        List<TableColumnInfoDTO> ltc = new ArrayList<>();
        tl.columnName = "id";
        tl.type = "INT";
        tl.comment = "主键";
        tl.isKey = "1";
        tl2.columnName = "name";
        tl2.comment = "test";
        tl2.isKey = "0";
        tl2.type = "VARCHAR(225)";
        tl3.columnName = "address";
        tl3.comment = "test address";
        tl3.isKey = "0";
        tl3.type = "VARCHAR(500)";
        ltc.add(tl);
        ltc.add(tl2);
        ltc.add(tl3);
        tab.columns = ltc;
        StringBuilder sql = new StringBuilder();
        String tableName = tab.tableName;
        sql.append("CREATE TABLE tableName");
        sql.append("(");
        StringBuilder sqlFileds=new StringBuilder();
        StringBuilder sqlAggregate=new StringBuilder("AGGREGATE KEY(");
        StringBuilder sqlSelectStrBuild=new StringBuilder();
        StringBuilder sqlDistributed=new StringBuilder("DISTRIBUTED BY HASH(");
        ltc.forEach((l) -> {
            if(l.isKey.equals("1"))
            {
                sqlDistributed.append(l.columnName);
            }
            sqlFileds.append(l.columnName+" "+ l.type+" comment "+"'"+l.comment+"' ,");
            sqlAggregate.append(l.columnName+",");
            sqlSelectStrBuild.append(l.columnName+",");
        });
        sqlDistributed.append(") BUCKETS 10");
        String aggregateStr=sqlAggregate.toString();
        aggregateStr=aggregateStr.substring(0,aggregateStr.lastIndexOf(","))+")";
        String selectStr=sqlSelectStrBuild.toString();
        selectStr=selectStr.substring(0,selectStr.lastIndexOf(","))+")";
        String filedStr=sqlFileds.toString();
        sql.append(filedStr.substring(0,filedStr.lastIndexOf(",")));
        String sqlSelectStr="select "+selectStr+" from "+tab.tableName;
/*        sql.append("id INT DEFAULT '10',,");
        sql.append("username VARCHAR(32) DEFAULT '',");
        sql.append("citycode SMALLINT");*/
        sql.append(")");
        sql.append(aggregateStr);
        sql.append(sqlDistributed.toString());
        //sql.append("AGGREGATE KEY(id, username,citycode )");
        //sql.append("DISTRIBUTED BY HASH("+hashKey+") BUCKETS 10");
        sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
        String stg_sql=sql.toString().replace("tableName",stg_table);
        String ods_sql=sql.toString().replace("tableName",ods_table);
        System.out.println(stg_sql);
        System.out.println(ods_sql);
        BusinessResult sqlResult_stg= doris.dorisBuildTable(stg_sql);
        BusinessResult sqlResult_ods= doris.dorisBuildTable(ods_sql);
        System.out.println(JSON.toJSONString(sqlResult_stg));
        System.out.println(JSON.toJSONString(sqlResult_ods));
        System.out.println(sqlSelectStr);
        BuildNifiFlowDTO bb = new BuildNifiFlowDTO();
        bb.appId = 123L;
        service.publishTask(TaskTypeEnum.BUILD_NIFI_FLOW.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_NIFI_FLOW,
                bb);
    }
    @Test
    public void testatlas(){
        String dataInfo="{\"appName\":\"api测试\",\"driveType\":\"mysql\",\"createUser\":\"yhxu\",\"appDes\":\"this is a tset instance\",\"host\":\"192.168.1.1\",\"port\":\"5200\",\"dbName\":\"yhxu_db\"}";
        AtlasEntityDTO ae = JSON.parseObject(dataInfo, AtlasEntityDTO.class);
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
        BusinessResult dbRes=atlas.atlasBuildDb(rdbms_db);
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
        BusinessResult proRes=atlas.atlasBuildProcess(entity_rdbms_process);
        System.out.println(proRes.msg);
    }
}
