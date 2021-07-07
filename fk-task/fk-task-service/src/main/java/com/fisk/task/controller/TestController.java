package com.fisk.task.controller;

import com.fisk.common.constants.MQConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.task.dto.atlas.TableColumnInfoDTO;
import com.fisk.task.dto.atlas.TableInfoDTO;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.utils.WsSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gy
 */
@RestController
@RequestMapping("/task")
@Slf4j
public class TestController {

    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    IBuildTaskService service;

    @GetMapping
    public void sendMsg(String msg) {
        System.out.println("主线程id：" + Thread.currentThread().getId());
        rabbitTemplate.convertAndSend(MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME, MQConstants.QueueConstants.BUILD_NIFI_FLOW, msg);
    }

    @GetMapping("/test")
    public void sendMsg(String exchange, String queue, String msg) {
        System.out.println("主线程id：" + Thread.currentThread().getId());
        rabbitTemplate.convertAndSend(exchange, queue, msg);
    }

    @GetMapping("/ws/sendMsg")
    public void wsSendMsg(String msg, Long id) {
        WsSessionManager.sendMsgById(msg, id);
    }

    @GetMapping("/ws/getOnlineCount")
    public int getOnlineCount() {
        return WsSessionManager.getOnlineCount();
    }

    @PostMapping("/testAtlasBuild")
    public void publishBuildAtlasTask() {
        TableInfoDTO tab = new TableInfoDTO();
        tab.userId = 37L;
        tab.tableName = "denny_table";
        TableColumnInfoDTO tl = new TableColumnInfoDTO();
        TableColumnInfoDTO tl2 = new TableColumnInfoDTO();
        TableColumnInfoDTO tl3 = new TableColumnInfoDTO();
        List<TableColumnInfoDTO> ltc = new ArrayList<>();
        tl.columnName = "id";
        tl.type = "INT";
        tl.comment = "主键";
        tl.isKey = "1";
        tl2.columnName = "name";
        tl2.comment = "test";
        tl2.isKey = "0";
        tl2.type = "VARCHAR(225)";
        tl3.columnName = "address";
        tl3.comment = "test address";
        tl3.isKey = "0";
        tl3.type = "VARCHAR(500)";
        ltc.add(tl);
        ltc.add(tl2);
        ltc.add(tl3);
        tab.columns = ltc;
        service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME, MQConstants.QueueConstants.BUILD_ATLAS_FLOW, tab);
    }
}
