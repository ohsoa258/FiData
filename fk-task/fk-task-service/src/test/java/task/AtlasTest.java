package task;
import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.service.IAtlasBuild;
import com.fisk.task.vo.ProcessGroupsVO;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.service.INifiFlowBuild;
import com.fisk.task.utils.NifiHelper;
import javafx.application.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
/**
 * Author:DennyHui
 * CreateTime: 2021/7/1 10:19
 * Description:
 */
import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FkTaskApplication.class)
@RunWith(SpringRunner.class)
public class AtlasTest {
    @Resource
    IAtlasBuild atlas;
@Test
    public void testdoriscreatetable(){
/*    StringBuilder sql = new StringBuilder();
    sql.append("CREATE TABLE denny_table");
    sql.append("{");
    sql.append("id INT DEFAULT '10',");
    sql.append("username VARCHAR(32) DEFAULT '',");
    sql.append("citycode SMALLINT,");
    sql.append("}");
    sql.append("AGGREGATE KEY(id, citycode, username)");
    sql.append("DISTRIBUTED BY HASH(id) BUCKETS 10");
    sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
    System.out.println(sql.toString());
    System.out.println(JSON.toJSONString(atlas.dorisBuildTable(sql.toString())));*/
    System.out.println(123);
}
}
