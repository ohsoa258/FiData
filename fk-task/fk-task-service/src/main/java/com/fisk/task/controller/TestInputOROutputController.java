package com.fisk.task.controller;

import com.davis.client.model.PortEntity;
import com.fisk.task.consumer.nifi.BuildNifiTaskListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@RestController
@RequestMapping("/inputPort")
@Slf4j
public class TestInputOROutputController {

    @Resource
    BuildNifiTaskListener listener;

    @PostMapping("/buildInputPort")
    public void buildInputPort() {
        PortEntity portEntity = listener.buildInputPorts();
    }
}
