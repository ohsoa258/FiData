package task;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import com.fisk.task.listener.mdm.BuildModelListener;
import net.minidev.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.fisk.task.utils.mdmBEBuild.impl.BuildPgCommandImpl.PUBLIC;

/**
 * @author WangYan
 * @date 2022/4/13 10:36
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FkTaskApplication.class)
@RunWith(SpringRunner.class)
public class TestBuildModel {

    DataSourceTypeEnum type = DataSourceTypeEnum.PG;
    String connectionStr = "jdbc:postgresql://192.168.1.250:5432/dmp_mdm_attributeLog?stringtype=unspecified";
    String acc = "postgres";
    String pwd = "Password01!";

    @Resource
    PublishTaskController publishTaskController;
    @Resource
    BuildModelListener buildModelListener;

    @Test
    public void Test1(){
        ModelDTO dto = new ModelDTO();
        dto.userId=60L;
        dto.setAttributeLogName("kkt");
        ResultEntity<Object> objectResultEntity = publishTaskController.pushModelByName(dto);
        System.out.println(objectResultEntity);
    }

    @Test
    public void Test2() throws SQLException {
        String tableName = "txy11";
        IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);

        AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
        Connection connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);
        String sql = sqlBuilder.buildAttributeLogTable(tableName);
        abstractDbHelper.executeSql(sql, connection);
        System.out.println("kkt创建成功！");
    }

    @Test
    public void aVoid(){
        EntityDTO dto = new EntityDTO();
        dto.setUserId(65L);
        dto.setEntityId(6);
        buildModelListener.backgroundCreateTasks(JSON.toJSONString(dto),null);
    }

    @Test
    public void test1(){
        System.out.println(this.buildAttributeLogTable("wangyanTest"));
    }

    public String buildAttributeLogTable(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE public." + tableName).append("(");
        str.append("ID int4 NOT NULL,");
        str.append("model_id int4 NULL,");
        str.append("entity_id int4 NULL,");
        str.append("attribute_id int4 NULL,");
        str.append("member_id int4 NULL,");
        str.append("batch_code VARCHAR ( 100 ) NULL").append(",");
        str.append("version_id int4 NULL,");
        str.append("old_code VARCHAR ( 200 ) NULL,");
        str.append("old_value VARCHAR ( 200 ) NULL,");
        str.append("new_code VARCHAR ( 200 ) NULL,");
        str.append("new_value VARCHAR ( 200 ) NULL,");
        str.append("create_time timestamp(6) NULL").append(",");
        str.append("create_user varchar(50) NULL").append(",");
        str.append("del_flag int2 NULL");
        str.append(");");
        return str.toString();
    }
}
