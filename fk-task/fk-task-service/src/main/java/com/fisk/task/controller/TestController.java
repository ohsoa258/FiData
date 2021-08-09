package com.fisk.task.controller;

import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.MessageLevelEnum;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.task.dto.doris.TableColumnInfoDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.utils.WsSessionManager;
import com.fisk.task.utils.YamlReader;
import fk.atlas.api.AtlasClient;
import fk.atlas.api.model.EnttityRdbmsInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

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
    @Resource
    DataAccessClient dc;

    @GetMapping
    public void sendMsg(String msg) {
        System.out.println("主线程id：" + Thread.currentThread().getId());
        rabbitTemplate.convertAndSend(MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME, MqConstants.QueueConstants.BUILD_NIFI_FLOW, msg);
    }

    @GetMapping("/test")
    public void sendMsg(String exchange, String queue, String msg) {
        System.out.println("主线程id：" + Thread.currentThread().getId());
        rabbitTemplate.convertAndSend(exchange, queue, msg);
    }

    @GetMapping("/catch")
    public void catchs() {
        String msg = null;
        msg.toString();
    }

    @GetMapping("/ws/sendMsg")
    public void wsSendMsg(String msg, Long id) {
        WsSessionManager.sendMsgById(msg, id, MessageLevelEnum.MEDIUM);
    }

    @GetMapping("/ws/getOnlineCount")
    public int getOnlineCount() {
        return WsSessionManager.getOnlineCount();
    }

    @PostMapping("/testAtlasBuild_Instance")
    public void publishBuildAtlasTask(@RequestBody EnttityRdbmsInstance.entity_rdbms_instance dataDTO) {
        //region 创建血缘关系
        String atlas_url = YamlReader.instance.getValueByKey("atlasconstr.url").toString();
        String atlas_username = YamlReader.instance.getValueByKey("atlasconstr.username").toString();
        String atlas_pwd = YamlReader.instance.getValueByKey("atlasconstr.password").toString();
        AtlasClient ac = new AtlasClient(atlas_url, atlas_username, atlas_pwd);
        String result = ac.CreateEntity_rdbms_instance(dataDTO);
        //endregion
    }

    @PostMapping("/test_GetAtlasDataInfo")
    public void publishBuildAtlasTask(@RequestParam("id") long id) {
        System.out.println(dc.getAtlasEntity(id));
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
        service.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(), MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME, MqConstants.QueueConstants.BUILD_DORIS_FLOW, tab);
    }
    @PostMapping("/testDorisBuildtable")
    public void publishBuildDorisTableTask() {
        DimensionAttributeAddDTO tab = new DimensionAttributeAddDTO();
        tab.dimensionId=8;
        tab.createType=1;
        tab.userId = 60L;
        service.publishTask(TaskTypeEnum.BUILD_DATAMODEL_DORIS_TABLE.getName(), MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME, MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE, tab);
    }
}
