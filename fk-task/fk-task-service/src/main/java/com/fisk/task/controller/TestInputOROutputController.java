package com.fisk.task.controller;

import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.PortEntity;
import com.fisk.task.consumer.nifi.BuildNifiTaskListener;
import com.fisk.task.dto.nifi.BuildConnectDTO;
import com.fisk.task.dto.nifi.BuildPortDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;

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

        String clientId = UUID.randomUUID().toString();
        System.out.println(clientId);

        String inputName = "aa";

        BuildPortDTO buildPortDTO = new BuildPortDTO();
        buildPortDTO.clientId = clientId;
        buildPortDTO.portName = inputName;
        buildPortDTO.componentId = "cbf61301-1011-117c-7765-c6d09a58fc4d";

        PortEntity portEntity = listener.buildInputPort(buildPortDTO);
        System.out.println(portEntity);
    }

    @PostMapping("/buildOutputPort")
    public void buildOutputPort() {

        String clientId = UUID.randomUUID().toString();
        System.out.println(clientId);

        String outputName = "aa";

        BuildPortDTO buildPortDTO = new BuildPortDTO();
        buildPortDTO.clientId = clientId;
        buildPortDTO.portName = outputName;
        buildPortDTO.componentId = "cbf61301-1011-117c-7765-c6d09a58fc4d";

        PortEntity portEntity = listener.buildOutputPort(buildPortDTO);
        System.out.println(portEntity);
    }

    @PostMapping("/buildInputPortConnections")
    public void buildInputPortConnections() {

        String clientId = UUID.randomUUID().toString();
        System.out.println(clientId);

        String outputName = "aa";

        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        buildConnectDTO.componentId = "cbf61301-1011-117c-7765-c6d09a58fc4d";

        ConnectionEntity connectionEntity = listener.buildInputPortConnections();
        System.out.println(connectionEntity);
    }

    @PostMapping("/buildOutPortPortConnections")
    public void buildOutPortPortConnections() {

        String clientId = UUID.randomUUID().toString();
        System.out.println(clientId);

        String outputName = "aa";

        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        buildConnectDTO.componentId = "cbf61301-1011-117c-7765-c6d09a58fc4d";

        ConnectionEntity connectionEntity = listener.buildOutPortPortConnections();
        System.out.println(connectionEntity);
    }


}
