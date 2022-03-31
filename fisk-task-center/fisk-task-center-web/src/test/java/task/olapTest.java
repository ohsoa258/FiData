package task;

import com.fisk.datamodel.client.DataModelClient;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.service.nifi.IOlap;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Author:yhxu
 * CreateTime: 2021/7/1 10:19
 * Description:
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FkTaskApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class olapTest {

    @Resource
    DataModelClient client;
    @Resource
    IOlap olap;


}
