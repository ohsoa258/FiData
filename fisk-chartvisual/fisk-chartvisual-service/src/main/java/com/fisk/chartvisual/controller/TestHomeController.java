package com.fisk.chartvisual.controller;

import com.netflix.discovery.DiscoveryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@Slf4j
public class TestHomeController {

    @GetMapping("/offline")
    public void offline() {
        DiscoveryManager.getInstance().shutdownComponent();
    }

    @GetMapping("/testAuthorization")
    public void testAuthorization() {

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
}
