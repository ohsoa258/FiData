package com.fisk.task;

import com.davis.client.model.ControllerServiceEntity;
import com.davis.client.model.PositionDTO;
import com.davis.client.model.ProcessGroupEntity;
import com.fisk.task.service.INifiComponentsBuild;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class NifiBuildTest {

    @Resource
    INifiComponentsBuild service;

    @Test
    public void buildGroup() {
        PositionDTO dto = new PositionDTO();
        dto.setX(300.00);
        dto.setY(300.00);
        service.buildProcessGroup("test", "", "017a10ae-82a2-134f-e9d1-3e45c0e5249b", dto);
    }

    @Test
    public void getGroup() {
        ProcessGroupEntity entity = service.getProcessGroupByPid("017a10ae-82a2-134f-e9d1-3e45c0e5249b");
        System.out.println(entity.toString());
    }

    @Test
    public void buildConnectionPool() {
        ControllerServiceEntity entity = service.buildProcessControlService("017a10ae-82a2-134f-e9d1-3e45c0e5249b", null);
        System.out.println(entity.toString());
    }
}
