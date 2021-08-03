package task;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.mapper.TBETLLogMapper;
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
    private TBETLLogMapper logMapper;
    @Resource
    private JdbcTemplate template;

    @Test
    public void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<TBETLlogPO> loglist = logMapper.selectList(null);
        //Assert.assertEquals(5, userList.size());
        loglist.forEach(System.out::println);
    }

    @DS("datainputdb")
    @Test
    public void dorisQuery() {
        List<TBETLlogPO> loglist = logMapper.selectList(null);
        //Assert.assertEquals(5, userList.size());
        loglist.forEach(System.out::println);
    }


    @DS("dorisdb")
    @Test
    public void jdbcTemp() {
        List<Map<String, Object>> list = template.queryForList("select * from non_ods_tb_nonrealtime_db013265562579919565 limit 10");
        System.out.println(list.toString());
    }
}
