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
}
