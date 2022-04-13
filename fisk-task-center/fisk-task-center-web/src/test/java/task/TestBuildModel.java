package task;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.FkTaskApplication;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.model.ModelDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/4/13 10:36
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FkTaskApplication.class)
@RunWith(SpringRunner.class)
public class TestBuildModel {

    @Resource
    PublishTaskController publishTaskController;

    @Test
    public void Test1(){
        ModelDTO dto = new ModelDTO();
        dto.setId(2022413);
        dto.userId=60L;
        dto.setAttributeLogName("kkt");
        ResultEntity<Object> objectResultEntity = publishTaskController.pushModel(dto);
        System.out.println(objectResultEntity);
    }
}
