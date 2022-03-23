package task;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.service.doris.IDorisIncrementalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/2 18:04
 * Description:
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FkTaskApplication.class)
@RunWith(SpringRunner.class)
@Component
public class doris {

    @Resource
    private JdbcTemplate template;
    @Resource
    private IDorisIncrementalService doris;

    @Test
    public void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        UpdateLogAndImportDataDTO dd=new UpdateLogAndImportDataDTO();
        dd.code="1488de4e-f431-11eb-8d50-0242ac110003";
        doris.updateNifiLogsAndImportOdsData(dd);
    }

    @DS("datainputdb")
    @Test
    public void dorisQuery() {

    }


    @DS("dorisdb")
    @Test
    public void jdbcTemp() {
        List<Map<String, Object>> list = template.queryForList("select * from non_ods_tb_nonrealtime_db013265562579919565 limit 10");
        System.out.println(list.toString());
    }
}
