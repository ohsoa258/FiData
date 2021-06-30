package com.fisk.chartvisual.controller;

import com.fisk.common.constants.SystemConstants;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.netflix.discovery.DiscoveryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/home")
@Slf4j
public class TestHomeController {

    @Resource
    UserHelper userInfo;
    @Resource
    PublishTaskClient publishTaskClient;

    @GetMapping("/offline")
    public void offline() {
        DiscoveryManager.getInstance().shutdownComponent();
    }

    @GetMapping("/testAuthorization")
    public String testAuthorization() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return request.getHeader("Authorization").replace(SystemConstants.AUTH_TOKEN_HEADER, "");
    }

    @GetMapping("/getLoginUserInfo")
    public UserInfo getLoginUserInfo() {
        return userInfo.getLoginUserInfo();
    }

    @GetMapping("/delay5s")
    public void delay5s() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/delay10s")
    public void delay10s() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/publishTask")
    public ResultEntity<Object> publishTask() {
        BuildNifiFlowDTO dto = new BuildNifiFlowDTO();
        dto.appId = 11L;
        dto.sendTime = LocalDateTime.now();
        dto.userId = userInfo.getLoginUserInfo().id;
        return publishTaskClient.publishBuildNifiFlowTask(dto);
    }
}
