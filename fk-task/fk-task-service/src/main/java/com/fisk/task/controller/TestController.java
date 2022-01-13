package com.fisk.task.controller;

import com.davis.client.ApiException;
import com.davis.client.model.ProcessorEntity;
import com.davis.client.model.ProcessorStatusDTO;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.MessageLevelEnum;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.task.dto.doris.TableColumnInfoDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.dto.olap.BuildCreateModelTaskDto;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.WsSessionManager;
import com.fisk.task.utils.YamlReader;
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
    @Resource
    INifiComponentsBuild iNifiComponentsBuild;

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
//        tab.dimensionId=23;
        tab.dimensionId=165;
        tab.createType=0;
        tab.userId = 60L;
        service.publishTask(TaskTypeEnum.BUILD_DATAMODEL_DORIS_TABLE.getName(), MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME, MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE, tab);
    }
    /**
     * 创建模型
     * @param
     * @return
     */
    @PostMapping("/testCreateModel")
    public void publishBuildAtomicKpiTask(){
        BuildCreateModelTaskDto buildCreateModelTaskDto=new BuildCreateModelTaskDto();
        buildCreateModelTaskDto.businessAreaId=1;
        buildCreateModelTaskDto.userId=60L;
         service.publishTask(TaskTypeEnum.BUILD_CREATEMODEL_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW,
                buildCreateModelTaskDto);
    }
    @PostMapping("/testNifi")
    public void testNifi(){
        try {

            ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor("ee11b478-017b-1000-20d7-4292a5b109c4");
            ProcessorStatusDTO status = processor.getStatus();
            List<ProcessorEntity> processorEntities = new ArrayList<>();
            List<ProcessorEntity> processorEntities1 = new ArrayList<>();
            processorEntities.add(processor);
            //先停止组件
            iNifiComponentsBuild.stopProcessor("",processorEntities);
            //修改调度
            processor.getComponent().getConfig().setSchedulingStrategy("");
            processor.getComponent().getConfig().setSchedulingPeriod("");
            processorEntities1.add(processor);
            iNifiComponentsBuild.updateProcessorConfig("",processorEntities1);
            //启动组件
            iNifiComponentsBuild.enabledProcessor("",processorEntities1);

           /* ProcessorStatusDTO processorStatusDTO = new ProcessorStatusDTO();
            processor.setStatus(processorStatusDTO);
            ProcessorDTO component = processor.getComponent();

            ProcessorConfigDTO config1 = component.getConfig();
            Map<String, String> properties = config1.getProperties();
            System.out.println("我擦"+properties);*/
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/testNifiFlow")
    public void publishBuildNifiFlowTask() {
        BuildNifiFlowDTO data=new BuildNifiFlowDTO();
        data.synchronousTypeEnum= SynchronousTypeEnum.TOPGODS;
        data.userId=60L;
        data.id=2036L;
        data.appId=691L;
        data.dataClassifyEnum = DataClassifyEnum.DATAACCESS;
         service.publishTask("创建表:"+data.tableName+"的数据流任务",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_NIFI_FLOW,
                data);
    }

    @PostMapping("/testDelete")
    public void publishBuildNifiFlowTaskDelete() {

        PgsqlDelTableDTO delTable = new PgsqlDelTableDTO();
        delTable.delApp = false;
        delTable.userId = 57L;
        delTable.appAtlasId = "";
        List<TableListDTO> tableList = new ArrayList<>();
        TableListDTO dto = new TableListDTO();
        dto.userId = 57L;
        dto.tableName = "cus_address1";
        tableList.add(dto);
        delTable.tableList = tableList;

        service.publishTask(TaskTypeEnum.BUILD_DATAINPUT_DELETE_PGSQL_STGTOODS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW,
                delTable);

    }


}
