package task;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.sql.Connection;
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
        IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);

        AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
        Connection connection = abstractDbHelper.connection(connectionStr, acc, pwd, type);
        String sql = sqlBuilder.buildAttributeLogTable(tableName);
        abstractDbHelper.executeSql(sql, connection);
        System.out.println("kkt创建成功！");
    }
}
