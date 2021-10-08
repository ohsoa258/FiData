package com.fisk.task.controller;

import com.davis.client.model.ConnectableDTO;
import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.PortEntity;
import com.fisk.task.dto.nifi.BuildConnectDTO;
import com.fisk.task.dto.nifi.BuildPortDTO;
import com.fisk.task.dto.nifi.NifiConnectDTO;
import com.fisk.task.service.INifiComponentsBuild;
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

    //    @Resource
//    BuildNifiTaskListener listener;
    @Resource
    INifiComponentsBuild listener;

    @PostMapping("/buildInputPort")
    public void buildInputPort() {

        String clientId = UUID.randomUUID().toString();
        System.out.println(clientId);

        String inputName = "appTest";

        BuildPortDTO buildPortDTO = new BuildPortDTO();
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
        NifiConnectDTO destination = new NifiConnectDTO();
        NifiConnectDTO source = new NifiConnectDTO();
        // 当前组件在哪个组下的组件id
        buildConnectDTO.fatherComponentId = "31162cce-017c-1000-c751-e72cc40134c3";
        destination.groupId = "31162cce-017c-1000-c751-e72cc40134c3";
        // input_port将连接的组件 id
//        buildConnectDTO.connectInPutPortComponentId = "31162e04-017c-1000-4d07-1efdabc64703";
        destination.id = "31162e04-017c-1000-4d07-1efdabc64703";
        destination.typeEnum = ConnectableDTO.TypeEnum.PROCESSOR;


        // input_port组件id
//        buildConnectDTO.inputPortComponentId = "31162ddb-017c-1000-66b4-7e788dca76ac";
        source.groupId = "31162cce-017c-1000-c751-e72cc40134c3";
        source.id = "31162ddb-017c-1000-66b4-7e788dca76ac";
        source.typeEnum = ConnectableDTO.TypeEnum.INPUT_PORT;

        // 目标组件类型
//        buildConnectDTO.destinationType = ConnectableDTO.TypeEnum.PROCESSOR;
        // 源组件地址
//        buildConnectDTO.sourceType = ConnectableDTO.TypeEnum.INPUT_PORT;

        buildConnectDTO.destination = destination;
        buildConnectDTO.source = source;

        ConnectionEntity connectionEntity = listener.buildInputPortConnections(buildConnectDTO);
        System.out.println(connectionEntity);
    }

    @PostMapping("/buildOutPortPortConnections")
    public void buildOutPortPortConnections() {

        String clientId = UUID.randomUUID().toString();
        System.out.println(clientId);

        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        NifiConnectDTO destination = new NifiConnectDTO();
        NifiConnectDTO source = new NifiConnectDTO();
        // 当前组件在哪个组下的组件id
        buildConnectDTO.fatherComponentId = "31162cce-017c-1000-c751-e72cc40134c3";
        destination.groupId = "31162cce-017c-1000-c751-e72cc40134c3";
        destination.id = "31163516-017c-1000-fed6-293a6aac300b";
        destination.typeEnum = ConnectableDTO.TypeEnum.OUTPUT_PORT;

        source.groupId = "31162cce-017c-1000-c751-e72cc40134c3";
        source.id = "31163439-017c-1000-34a8-09ae3a527bf3";
        source.typeEnum = ConnectableDTO.TypeEnum.PROCESSOR;
/*

        // output_port组件id
        buildConnectDTO.outputPortComponentId = "31163516-017c-1000-fed6-293a6aac300b";
        // 连接output_port的组件 id
        buildConnectDTO.connectOutPutPortComponentId = "31163439-017c-1000-34a8-09ae3a527bf3";
        // 目标组件类型
        buildConnectDTO.destinationType = ConnectableDTO.TypeEnum.OUTPUT_PORT;
        // 源组件地址
        buildConnectDTO.sourceType = ConnectableDTO.TypeEnum.PROCESSOR;
*/

        buildConnectDTO.destination = destination;
        buildConnectDTO.source = source;
        ConnectionEntity connectionEntity = listener.buildOutPortPortConnections(buildConnectDTO);
        System.out.println(connectionEntity);
    }


}
