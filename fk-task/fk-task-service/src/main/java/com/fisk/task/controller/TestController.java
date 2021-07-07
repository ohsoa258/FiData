package com.fisk.task.controller;

import com.fisk.common.constants.MQConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.task.dto.atlas.TableColumnInfoDTO;
import com.fisk.task.dto.atlas.TableInfoDTO;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.utils.WsSessionManager;
import com.fisk.task.utils.YamlReader;
import fk.atlas.api.AtlasClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static fk.atlas.api.model.EnttityRdbmsInstance.*;

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
        //region 创建血缘关系
        String atlas_url = YamlReader.instance.getValueByKey("atlasconstr.url").toString();
        String atlas_username = YamlReader.instance.getValueByKey("atlasconstr.username").toString();
        String atlas_pwd = YamlReader.instance.getValueByKey("atlasconstr.password").toString();
        AtlasClient ac = new AtlasClient(atlas_url, atlas_username, atlas_pwd);
        attributes_rdbms_instance ari = new attributes_rdbms_instance();
        attributes_field_rdbms_instance arif = new attributes_field_rdbms_instance();
        arif.qualifiedName = "doris_instance@atlas";
        arif.name = "yhxu_instance";
        arif.rdbms_type = "mysql";
        arif.platform = "Linux";
        arif.hostname = "192.168.1.520";
        arif.port = "3306";
        arif.protocol = "mysql protocal";
        arif.contact_info = "your contact info";
        arif.description = " yhxu java create mysql instance intity";
        arif.owner = "root";
        arif.ownerName = "root";
        ari.attributes = arif;
        entity_rdbms_instance eri = new entity_rdbms_instance();
        eri.entity = ari;
        String result = ac.CreateEntity_rdbms_instance(eri);
        //endregion
    }
    @PostMapping("/testDorisBuild")
    public void publishBuildDorisTask() {
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
        service.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(), MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME, MQConstants.QueueConstants.BUILD_DORIS_FLOW, tab);
    }
}
