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

        String inputName = "appTest";

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

        String outputName = "appTest";

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

        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        // 当前组件在哪个组下的组件id
        buildConnectDTO.fatherComponentId = "cbf61301-1011-117c-7765-c6d09a58fc4d";
        // input_port将连接的组件 id
        buildConnectDTO.connectInPutPortComponentId = "cbf61303-1011-117c-3f2b-d876c6f14e97";
        // input_port组件id
        buildConnectDTO.inputPortComponentId = "26289101-017c-1000-3be7-f1718c728a2f";

        ConnectionEntity connectionEntity = listener.buildInputPortConnections(buildConnectDTO);
        System.out.println(connectionEntity);
    }

    @PostMapping("/buildOutPortPortConnections")
    public void buildOutPortPortConnections() {

        String clientId = UUID.randomUUID().toString();
        System.out.println(clientId);

        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        // 当前组件在哪个组下的组件id
        buildConnectDTO.fatherComponentId = "cbf61301-1011-117c-7765-c6d09a58fc4d";
        // output_port组件id
        buildConnectDTO.outputPortComponentId = "26cdf6f5-017c-1000-c76b-8bfdb0c150a8";
        // 连接output_port的组件 id
        buildConnectDTO.connectOutPutPortComponentId = "cbf61303-1011-117c-3f2b-d876c6f14e97";

        ConnectionEntity connectionEntity = listener.buildOutPortPortConnections(buildConnectDTO);
        System.out.println(connectionEntity);
    }


}
