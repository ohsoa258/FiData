package com.fisk.task.controller;

import com.fisk.common.constants.MQConstants;
import com.fisk.task.utils.WsSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author gy
 */
@RestController
@RequestMapping("/task")
@Slf4j
public class TestController {

    @Resource
    RabbitTemplate rabbitTemplate;

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
}
