package task;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.doris.TableColumnInfoDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.dto.olap.BuildCreateModelTaskDto;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuildInstance;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.service.IDorisBuild;
import com.fisk.task.service.IOlap;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
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
@Slf4j
public class olapTest {

    @Resource
    DataModelClient client;
    @Resource
    IOlap olap;


}
