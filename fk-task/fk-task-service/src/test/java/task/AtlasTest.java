package task;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MQConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.dto.doris.TableColumnInfoDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.service.IDorisBuild;
import com.fisk.task.service.IBuildTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    IDorisBuild atlas;
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
        BusinessResult sqlResult_stg= atlas.dorisBuildTable(stg_sql);
        BusinessResult sqlResult_ods= atlas.dorisBuildTable(ods_sql);
        System.out.println(JSON.toJSONString(sqlResult_stg));
        System.out.println(JSON.toJSONString(sqlResult_ods));
        System.out.println(sqlSelectStr);
        BuildNifiFlowDTO bb = new BuildNifiFlowDTO();
        bb.appId = 123L;
        service.publishTask(TaskTypeEnum.BUILD_NIFI_FLOW.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_NIFI_FLOW,
                bb);
    }
}
