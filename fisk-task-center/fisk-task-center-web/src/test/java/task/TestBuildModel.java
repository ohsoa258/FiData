package task;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.utils.AbstractDbHelper;
import com.fisk.task.utils.IBuildFactoryHelper;
import com.fisk.task.utils.buildSql.IBuildSqlCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        IBuildSqlCommand sqlBuilder = IBuildFactoryHelper.getSqlBuilder(type);

        AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
        Connection connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);
        String sql = sqlBuilder.buildAttributeLog(tableName);
        abstractDbHelper.executeSql(sql, connection);
        System.out.println("kkt创建成功！");
    }
}
