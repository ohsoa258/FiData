package com.fisk.task.consumer.nifi;

import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.common.enums.task.nifi.ControllerServiceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.datafactory.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsDTO;
import com.fisk.datafactory.dto.tasknifi.PortRequestParamDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.task.consumer.doris.BuildDataModelDorisTableListener;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.nifi.FunnelDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.*;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.enums.PortComponentEnum;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.service.impl.*;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.NifiPositionHelper;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;


@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_CUSTOMWORK_FLOW)
@Slf4j
public class BuildNifiCustomWorkFlow {


    @Resource
    INifiComponentsBuild componentsBuild;
    @Resource
    AppNifiSettingServiceImpl appNifiSettingService;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    PublishTaskController publishTaskController;
    @Resource
    BuildDataModelDorisTableListener buildDataModelDorisTableListener;
    @Resource
    NifiConfigServiceImpl nifiConfigService;
    @Resource
    NifiSchedulingComponentImpl nifiSchedulingComponent;
    @Resource
    PipelineConfigurationImpl pipelineConfiguration;
    @Resource
    NotifyConfigurationImpl notifyConfiguration;
    @Resource
    BuildNifiTaskListener buildNifiTaskListener;
    @Value("${spring.redis.host}")
    private String redisHost;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    OlapMapper olapMapper;
    @Resource
    NifiComponentsBuildImpl nifiComponentsBuild;


    @RabbitHandler
    @MQConsumerLog
    public void msg(String data, Channel channel, Message message) {
        // 组里共用port,用漏斗连接共同使用,这样向外提供的连接点就只有一个
        NifiCustomWorkListDTO dto = JSON.parseObject(data, NifiCustomWorkListDTO.class);
        log.info("管道参数::" + dto);
        dto.structure = stringToMap(dto.structure1);
        dto.externalStructure = stringToMap(dto.externalStructure1);
        //多次发布,重建管道
        deleteCustomWorkNifiFlow(dto);
        //1.建组结构--对应表tb_app_nifi_setting存组结构
        String groupStructure = createGroupStructure(dto);
        //2.建立流程--根据类别建立对应的流程
        createCustomWorkNifiFlow(dto, groupStructure);
        //先外部后里面
        createNotifyProcessor(groupStructure, dto.externalStructure, 1);
        createNotifyProcessor(groupStructure, dto.structure, 2);
        //4.连线--根据流程关系连线
        createConnectingLine(dto);
        //启动
        try {

            ScheduleComponentsEntity scheduleComponentsEntity = new ScheduleComponentsEntity();
            scheduleComponentsEntity.setId(groupStructure);
            scheduleComponentsEntity.setDisconnectedNodeAcknowledged(false);
            scheduleComponentsEntity.setState(ScheduleComponentsEntity.StateEnum.RUNNING);
            //启动两次,防止有的组件创建不及时导致没启动
            for (int i = 3; i > 0; i--) {
                Thread.sleep(200);
                NifiHelper.getFlowApi().scheduleComponents(groupStructure, scheduleComponentsEntity);
            }
        } catch (ApiException | InterruptedException e) {
            log.info("此组启动失败:" + groupStructure);
        }

    }

    private void deleteCustomWorkNifiFlow(NifiCustomWorkListDTO nifiCustomWorkListDTO) {
        //tb_app_nifi_setting. tb_table_nifi_setting. tb_nifi_scheduling_component. tb_pipeline_configuration
        //重复发布需要处理这四张表里的老数据
        //暂停原流程   bx
        String appComponentId = "";
        List<AppNifiSettingPO> appNifiSettingPOList = appNifiSettingService.query().eq("app_id", nifiCustomWorkListDTO.nifiCustomWorkflowId).list();
        for (int i = 0; i < appNifiSettingPOList.size(); i++) {
            if (appNifiSettingPOList.get(i).nifiCustomWorkflowId != null) {
                appComponentId = appNifiSettingPOList.get(i).appComponentId;
            }
        }

        try {
            //停止
            ScheduleComponentsEntity scheduleComponentsEntity = new ScheduleComponentsEntity();
            scheduleComponentsEntity.setId(appComponentId);
            scheduleComponentsEntity.setDisconnectedNodeAcknowledged(false);
            scheduleComponentsEntity.setState(ScheduleComponentsEntity.StateEnum.STOPPED);
            NifiHelper.getFlowApi().scheduleComponents(appComponentId, scheduleComponentsEntity);
            //清空队列
            nifiComponentsBuild.emptyNifiConnectionQueue(appComponentId);
            //获取大组的控制器服务   includeancestorgroups includedescendantgroups
            ControllerServicesEntity controllerServicesFromGroup = NifiHelper.getFlowApi().getControllerServicesFromGroup(appComponentId, true, true);
            List<ControllerServiceEntity> controllerServices = controllerServicesFromGroup.getControllerServices();
            NifiConfigPO one = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName()).one();
            String componentId = one.componentId;
            for (ControllerServiceEntity controllerServiceEntity : controllerServices) {
                //保留配置库服务
                if (!Objects.equals(controllerServiceEntity.getId(), componentId)) {
                    //除去被关联的服务
                    if (!Objects.equals(controllerServiceEntity.getComponent().getType(), ControllerServiceTypeEnum.REDISCONNECTIONPOOL.getName())) {
                        nifiComponentsBuild.controllerServicesRunStatus(controllerServiceEntity.getId());
                    }
                }
            }
            for (ControllerServiceEntity controllerServiceEntity : controllerServices) {
                //保留配置库服务
                if (!Objects.equals(controllerServiceEntity.getId(), componentId)) {
                    //删除被关联的服务
                    if (Objects.equals(controllerServiceEntity.getComponent().getType(), ControllerServiceTypeEnum.REDISCONNECTIONPOOL.getName())) {
                        nifiComponentsBuild.controllerServicesRunStatus(controllerServiceEntity.getId());
                    }
                }
            }
            ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(appComponentId);
            NifiHelper.getProcessGroupsApi().removeProcessGroup(appComponentId, String.valueOf(processGroup.getRevision().getVersion()), processGroup.getRevision().getClientId(), false);


            //emptyNifiConnectionQueue
        } catch (ApiException e) {
            log.info("此组删除失败:" + appComponentId);
        }

        List<AppNifiSettingPO> appNifiSettingPOS = appNifiSettingService.query().eq("nifi_custom_workflow_id", nifiCustomWorkListDTO.nifiCustomWorkflowId)
                .eq("type", DataClassifyEnum.CUSTOMWORKSTRUCTURE.getValue()).list();
        List<NifiCustomWorkDTO> nifiCustomWorkDTOS = nifiCustomWorkListDTO.nifiCustomWorkDTOS;
        Map<String, Object> Map1 = new HashMap<>();
        Map<String, Object> Map2 = new HashMap<>();
        Map<String, Object> Map3 = new HashMap<>();
        Map<String, Object> Map4 = new HashMap<>();
        for (NifiCustomWorkDTO nifiCustomWorkDTO : nifiCustomWorkDTOS) {
            BuildNifiCustomWorkFlowDTO nifiNode = nifiCustomWorkDTO.NifiNode;
            if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT)) {
                //tb_nifi_scheduling_component
                Map1.put("nifi_custom_workflow_detail_id", nifiNode.workflowDetailId);
                nifiSchedulingComponent.removeByMap(Map1);
            } else if (!Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT) && !Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                Map4.put("nifi_custom_workflow_detail_id", nifiNode.workflowDetailId);
                tableNifiSettingService.removeByMap(Map4);
            }
        }
        for (AppNifiSettingPO appNifiSettingPO1 : appNifiSettingPOS) {
            Map2.put("app_id", appNifiSettingPO1.appId);
            pipelineConfiguration.removeByMap(Map2);
        }
        Map3.put("nifi_custom_workflow_id", nifiCustomWorkListDTO.nifiCustomWorkflowId);
        appNifiSettingService.removeByMap(Map3);

    }

    private Map stringToMap(String mapString) {
        Map<Map, Map> map = new HashMap<>();
        if (mapString.length() > 2) {
            mapString = mapString.substring(1, mapString.length() - 2);
            String[] split = mapString.split("},");
            String[] split1 = new String[2];
            Map<Map<String, String>, Map<String, String>> map1 = new HashMap<>();
            Map<Map<String, String>, Map<String, String>> map2 = new HashMap<>();
            Map<Map<String, String>, Map<String, String>> map3 = new HashMap<>();
            Gson gson = new Gson();
            for (String s : split) {
                map1 = map3;
                map2 = map3;
                s += "}";
                //{123=cmd}={643=job1, 644=job2}
                split1 = s.split("}=");
                split1[0] += "}";
                map1 = gson.fromJson(split1[0], Map.class);
                map2 = gson.fromJson(split1[1], Map.class);
                map.put(map1, map2);
            }
        }
        return map;
    }


    private String createGroupStructure(NifiCustomWorkListDTO nifiCustomWorkListDTO) {
        String groupId = null;
        String groupPid = null;
        String groupName = null;
        String groupPname = null;
        String greatProcessGroupEntityId = "";
        BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
        dto.details = nifiCustomWorkListDTO.pipelineName;
        dto.name = nifiCustomWorkListDTO.pipelineName;
        //问题一
        NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PIPELINE_NIFI_FLOW_GROUP_ID.getName()).one();
        if(nifiConfigPO!=null){
            dto.groupId=nifiConfigPO.componentId;
        }else{
            BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
            buildProcessGroupDTO.name=ComponentIdTypeEnum.PIPELINE_NIFI_FLOW_GROUP_ID.getName();
            buildProcessGroupDTO.details=ComponentIdTypeEnum.PIPELINE_NIFI_FLOW_GROUP_ID.getName();
            int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
            buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
            BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
            if (processGroupEntityBusinessResult.success) {
                dto.groupId=processGroupEntityBusinessResult.data.getId();
                NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                nifiConfigPO1.componentId=dto.groupId;
                nifiConfigPO1.componentKey=ComponentIdTypeEnum.PIPELINE_NIFI_FLOW_GROUP_ID.getName();
                nifiConfigService.save(nifiConfigPO1);
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processGroupEntityBusinessResult.msg);
            }
        }
        int count = componentsBuild.getGroupCount(dto.groupId);
        dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
        //创建顶级组
        BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
        if (!res.success) {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
        }
        //向appNifiSettingService保存顶级组
        greatProcessGroupEntityId = res.data.getId();
        AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
        appNifiSettingPO.appId = nifiCustomWorkListDTO.nifiCustomWorkflowId;
        appNifiSettingPO.type = DataClassifyEnum.CUSTOMWORKSTRUCTURE.getValue();
        appNifiSettingPO.appComponentId = res.data.getId();
        appNifiSettingPO.nifiCustomWorkflowId = nifiCustomWorkListDTO.nifiCustomWorkflowId;
        appNifiSettingService.save(appNifiSettingPO);
        createGroupPipeline(appNifiSettingPO, nifiCustomWorkListDTO.pipelineName);
        //外部组
        Map<Map, Map> externalStructure = nifiCustomWorkListDTO.externalStructure;
        Iterator<Map.Entry<Map, Map>> externalStructureMap = externalStructure.entrySet().iterator();
        while (externalStructureMap.hasNext()) {
            //(父,子)
            Map.Entry<Map, Map> entry = externalStructureMap.next();
            log.info("父对象 = " + entry.getKey() + ", 子对象 = " + entry.getValue());
            Map<String, String> node = entry.getValue();
            Iterator<Map.Entry<String, String>> nodeMap = node.entrySet().iterator();
            while (nodeMap.hasNext()) {
                Map.Entry<String, String> nodeEntry = nodeMap.next();
                groupId = nodeEntry.getKey();
                groupName = nodeEntry.getValue();
                log.info("子对象-id = " + nodeEntry.getKey() + ", 子对象-名称 = " + nodeEntry.getValue());
                //找父id,建立子组
                AppNifiSettingPO one = appNifiSettingService.query().eq("app_id", nifiCustomWorkListDTO.nifiCustomWorkflowId).eq("nifi_custom_workflow_id", nifiCustomWorkListDTO.nifiCustomWorkflowId).eq("type", DataClassifyEnum.CUSTOMWORKSTRUCTURE.getValue()).one();
                //建立子组
                dto.details = groupName;
                dto.name = groupName;
                int count1 = componentsBuild.getGroupCount(one.appComponentId);
                dto.groupId = one.appComponentId;
                dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count1);
                //创建层级组
                BusinessResult<ProcessGroupEntity> res1 = componentsBuild.buildProcessGroup(dto);
                if (!res1.success) {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
                appNifiSettingPO.appId = String.valueOf(groupId);
                appNifiSettingPO.appPid = nifiCustomWorkListDTO.nifiCustomWorkflowId;
                appNifiSettingPO.type = DataClassifyEnum.CUSTOMWORKSTRUCTURE.getValue();
                appNifiSettingPO.nifiCustomWorkflowId = nifiCustomWorkListDTO.nifiCustomWorkflowId;
                appNifiSettingPO.appComponentId = res1.data.getId();
                appNifiSettingService.save(appNifiSettingPO);
                // 在层级组里生成port和漏斗,后面连接,还需要存库
                BuildPortDTO buildPortDTO = new BuildPortDTO();
                buildPortDTO.componentX = 500.0;
                buildPortDTO.componentY = 500.0;
                buildPortDTO.componentId = appNifiSettingPO.appComponentId;
                buildPortDTO.portName = groupName;
                PortEntity inputPort = componentsBuild.buildInputPort(buildPortDTO);
                PortEntity outputPort = componentsBuild.buildOutputPort(buildPortDTO);
                FunnelDTO funnelDTO = new FunnelDTO();
                funnelDTO.groupId = buildPortDTO.componentId;
                BusinessResult<FunnelEntity> funnel1 = componentsBuild.createFunnel(funnelDTO);
                BusinessResult<FunnelEntity> funnel2 = componentsBuild.createFunnel(funnelDTO);
                PipelineConfigurationPO pipelineConfigurationPO = new PipelineConfigurationPO();
                pipelineConfigurationPO.app_id = appNifiSettingPO.appId;
                pipelineConfigurationPO.appComponentId = appNifiSettingPO.appComponentId;
                pipelineConfigurationPO.outputPortId = outputPort.getId();
                pipelineConfigurationPO.inputPortId = inputPort.getId();
                pipelineConfigurationPO.inFunnelId = funnel1.data.getId();
                pipelineConfigurationPO.outFunnelId = funnel2.data.getId();
                pipelineConfiguration.save(pipelineConfigurationPO);
            }

        }

        Map<Map, Map> map = nifiCustomWorkListDTO.structure;
        Iterator<Map.Entry<Map, Map>> entries = map.entrySet().iterator();
        //第一层为上面这一个组
        while (entries.hasNext()) {
            //(父,子)
            Map.Entry<Map, Map> entry = entries.next();
            log.info("父对象 = " + entry.getKey() + ", 子对象 = " + entry.getValue());
            Map<String, String> fnode = entry.getKey();
            Map<String, String> node = entry.getValue();
            Iterator<Map.Entry<String, String>> entries1 = fnode.entrySet().iterator();
            while (entries1.hasNext()) {
                Map.Entry<String, String> entry1 = entries1.next();
                groupPid = entry1.getKey();
                groupPname = entry1.getValue();
                log.info("父对象-id = " + entry1.getKey() + ", 父对象-名称 = " + entry1.getValue());
            }
            Iterator<Map.Entry<String, String>> entries2 = node.entrySet().iterator();
            while (entries2.hasNext()) {
                Map.Entry<String, String> entry2 = entries2.next();
                groupId = entry2.getKey();
                groupName = entry2.getValue();
                log.info("子对象-id = " + entry2.getKey() + ", 子对象-名称 = " + entry2.getValue());
                //找父id,建立子组
                AppNifiSettingPO one = appNifiSettingService.query().eq("app_id", groupPid).eq("nifi_custom_workflow_id", nifiCustomWorkListDTO.nifiCustomWorkflowId).eq("type", DataClassifyEnum.CUSTOMWORKSTRUCTURE.getValue()).one();
                //建立子组
                dto.details = groupName;
                dto.name = groupName;
                int count1 = componentsBuild.getGroupCount(one.appComponentId);
                dto.groupId = one.appComponentId;
                dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count1);
                //创建层级组
                BusinessResult<ProcessGroupEntity> res1 = componentsBuild.buildProcessGroup(dto);
                if (!res1.success) {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
                appNifiSettingPO.appId = String.valueOf(groupId);
                appNifiSettingPO.appPid = String.valueOf(groupPid);
                appNifiSettingPO.nifiCustomWorkflowId = nifiCustomWorkListDTO.nifiCustomWorkflowId;
                appNifiSettingPO.type = DataClassifyEnum.CUSTOMWORKSTRUCTURE.getValue();
                appNifiSettingPO.appComponentId = res1.data.getId();
                appNifiSettingService.save(appNifiSettingPO);
                // 在层级组里生成port和漏斗,后面连接,还需要存库
                BuildPortDTO buildPortDTO = new BuildPortDTO();
                buildPortDTO.componentX = 500.0;
                buildPortDTO.componentY = 500.0;
                buildPortDTO.componentId = appNifiSettingPO.appComponentId;
                buildPortDTO.portName = groupName;
                PortEntity inputPort = componentsBuild.buildInputPort(buildPortDTO);
                PortEntity outputPort = componentsBuild.buildOutputPort(buildPortDTO);
                FunnelDTO funnelDTO = new FunnelDTO();
                funnelDTO.groupId = buildPortDTO.componentId;
                BusinessResult<FunnelEntity> funnel1 = componentsBuild.createFunnel(funnelDTO);
                BusinessResult<FunnelEntity> funnel2 = componentsBuild.createFunnel(funnelDTO);
                PipelineConfigurationPO pipelineConfigurationPO = new PipelineConfigurationPO();
                pipelineConfigurationPO.app_id = appNifiSettingPO.appId;
                pipelineConfigurationPO.appComponentId = appNifiSettingPO.appComponentId;
                pipelineConfigurationPO.outputPortId = outputPort.getId();
                pipelineConfigurationPO.inputPortId = inputPort.getId();
                pipelineConfigurationPO.inFunnelId = funnel1.data.getId();
                pipelineConfigurationPO.outFunnelId = funnel2.data.getId();
                pipelineConfiguration.save(pipelineConfigurationPO);
            }
        }
        return greatProcessGroupEntityId;
    }

    private void createGroupPipeline(AppNifiSettingPO appNifiSettingPO, String groupName) {
        BuildPortDTO buildPortDTO = new BuildPortDTO();
        buildPortDTO.componentX = 500.0;
        buildPortDTO.componentY = 500.0;
        buildPortDTO.componentId = appNifiSettingPO.appComponentId;
        buildPortDTO.portName = groupName;
        PortEntity inputPort = componentsBuild.buildInputPort(buildPortDTO);
        PortEntity outputPort = componentsBuild.buildOutputPort(buildPortDTO);
        FunnelDTO funnelDTO = new FunnelDTO();
        funnelDTO.groupId = buildPortDTO.componentId;
        BusinessResult<FunnelEntity> funnel1 = componentsBuild.createFunnel(funnelDTO);
        BusinessResult<FunnelEntity> funnel2 = componentsBuild.createFunnel(funnelDTO);
        PipelineConfigurationPO pipelineConfigurationPO = new PipelineConfigurationPO();
        pipelineConfigurationPO.app_id = appNifiSettingPO.appId;
        pipelineConfigurationPO.appComponentId = appNifiSettingPO.appComponentId;
        pipelineConfigurationPO.outputPortId = outputPort.getId();
        pipelineConfigurationPO.inputPortId = inputPort.getId();
        pipelineConfigurationPO.inFunnelId = funnel1.data.getId();
        pipelineConfigurationPO.outFunnelId = funnel2.data.getId();
        pipelineConfiguration.save(pipelineConfigurationPO);
    }

    private List<AppNifiSettingDTO> createCustomWorkNifiFlow(NifiCustomWorkListDTO nifiCustomWorkListDTO, String groupStructure) {
        BuildNifiCustomWorkFlowDTO nifiNode = new BuildNifiCustomWorkFlowDTO();
        TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
        List<AppNifiSettingDTO> appNifiSettingDTOS = new ArrayList<>();
        AppNifiSettingDTO appNifiSettingDTO = new AppNifiSettingDTO();
        String groupId = null;
        //1.找到在哪个组下面
        List<NifiCustomWorkDTO> nifiCustomWorkDTOS = nifiCustomWorkListDTO.nifiCustomWorkDTOS;
        for (NifiCustomWorkDTO nifiCustomWorkDTO : nifiCustomWorkDTOS) {
            if (Objects.equals(nifiCustomWorkDTO.NifiNode.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                continue;
            }
            nifiNode = nifiCustomWorkDTO.NifiNode;
            AppNifiSettingPO one = appNifiSettingService.query().eq("app_id", nifiNode.groupId).eq("nifi_custom_workflow_id", nifiCustomWorkListDTO.nifiCustomWorkflowId).eq("type", DataClassifyEnum.CUSTOMWORKSTRUCTURE.getValue()).one();
            //组id在这里
            log.info("父级id:" + nifiNode.groupId);
            groupId = one.appComponentId;
            //2.拼装参数,三类nifi流程,3.调用方法生成流程
            if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKDATAACCESS)) {
                BuildNifiFlowDTO buildNifiFlowDTO = new BuildNifiFlowDTO();
                TableNifiSettingPO one1 = tableNifiSettingService.query().eq("table_access_id", nifiNode.tableId).eq("type", OlapTableEnum.PHYSICS.getValue()).eq("del_flag", 1).one();
                tableNifiSettingPO.appId = Math.toIntExact(one1.appId);
                tableNifiSettingPO.tableName = one1.tableName;
                tableNifiSettingPO.tableAccessId = Integer.valueOf(nifiNode.tableId);
                tableNifiSettingPO.selectSql = one1.selectSql;
                tableNifiSettingPO.nifiCustomWorkflowDetailId = String.valueOf(nifiNode.workflowDetailId);
                tableNifiSettingPO.type = OlapTableEnum.CUSTOMWORKPHYSICS.getValue();
                tableNifiSettingService.save(tableNifiSettingPO);
                buildNifiFlowDTO.id = Long.valueOf(nifiNode.tableId);
                buildNifiFlowDTO.appId = Long.valueOf(one1.appId);
                buildNifiFlowDTO.synchronousTypeEnum = SynchronousTypeEnum.TOPGODS;
                buildNifiFlowDTO.type = OlapTableEnum.CUSTOMWORKPHYSICS;
                buildNifiFlowDTO.dataClassifyEnum = DataClassifyEnum.CUSTOMWORKDATAACCESS;
                buildNifiFlowDTO.groupComponentId = groupId;
                buildNifiFlowDTO.tableName = one1.tableName;
                buildNifiFlowDTO.userId = nifiCustomWorkListDTO.userId;
                buildNifiFlowDTO.workflowDetailId = String.valueOf(nifiNode.workflowDetailId);
                buildNifiFlowDTO.nifiCustomWorkflowId = nifiCustomWorkListDTO.nifiCustomWorkflowId;
                buildNifiFlowDTO.groupStructureId = groupStructure;
                buildNifiTaskListener.msg(JSON.toJSONString(buildNifiFlowDTO), null, null);
                //publishTaskController.publishBuildNifiFlowTask(buildNifiFlowDTO);

                /*appNifiSettingDTO.appId = String.valueOf(one1.appId);
                appNifiSettingDTO.appPid = String.valueOf(nifiNode.groupId);
                appNifiSettingDTO.tableId = Integer.valueOf(nifiNode.tableId);
                appNifiSettingDTO.type = nifiNode.type.getValue();
                appNifiSettingDTO.tableType = OlapTableEnum.CUSTOMWORKPHYSICS;
                appNifiSettingDTOS.add(appNifiSettingDTO);*/
            } else if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKDATAMODELING)) {
                TableNifiSettingPO one1 = new TableNifiSettingPO();
                if (Objects.equals(nifiNode.tableType, OlapTableEnum.CUSTOMWORKDIMENSION)) {
                    one1 = tableNifiSettingService.query().eq("table_access_id", nifiNode.tableId).eq("type", OlapTableEnum.DIMENSION.getValue()).eq("del_flag", 1).one();
                } else if (Objects.equals(nifiNode.tableType, OlapTableEnum.CUSTOMWORKFACT)) {
                    one1 = tableNifiSettingService.query().eq("table_access_id", nifiNode.tableId).eq("type", OlapTableEnum.FACT.getValue()).eq("del_flag", 1).one();
                }
                //维度,事实流程 panxin
                /*ModelMetaDataDTO modelMetaDataDTO = new ModelMetaDataDTO();
                modelMetaDataDTO.id = Long.parseLong(nifiNode.tableId);
                modelMetaDataDTO.appId = Math.toIntExact(nifiNode.appId);
                modelMetaDataDTO.tableName = nifiNode.tableName;
                modelMetaDataDTO.sqlName = one1.selectSql.substring(4);
                modelMetaDataDTO.groupComponentId = groupId;*/
                ModelPublishDataDTO modelPublishDataDTO = new ModelPublishDataDTO();
                modelPublishDataDTO.businessAreaId = one1.appId;
                modelPublishDataDTO.nifiCustomWorkflowId = nifiCustomWorkListDTO.nifiCustomWorkflowId;
                ModelPublishTableDTO modelPublishTableDTO = new ModelPublishTableDTO();
                modelPublishTableDTO.tableName = one1.tableName;
                modelPublishTableDTO.groupComponentId = groupId;
                modelPublishTableDTO.tableId = Long.parseLong(nifiNode.tableId);
                modelPublishTableDTO.nifiCustomWorkflowDetailId = String.valueOf(nifiNode.workflowDetailId);
                modelPublishTableDTO.groupStructureId = groupStructure;
                log.info("表id为:" + nifiNode.tableId);
                if (Objects.equals(nifiNode.tableType, OlapTableEnum.CUSTOMWORKDIMENSION)) {

                    buildDataModelDorisTableListener.createNiFiFlow(modelPublishDataDTO, modelPublishTableDTO, nifiNode.tableName, nifiNode.type, OlapTableEnum.CUSTOMWORKDIMENSION);
                    appNifiSettingDTO.tableType = OlapTableEnum.CUSTOMWORKDIMENSION;
                } else if (Objects.equals(nifiNode.tableType, OlapTableEnum.CUSTOMWORKFACT)) {
                    buildDataModelDorisTableListener.createNiFiFlow(modelPublishDataDTO, modelPublishTableDTO, nifiNode.tableName, nifiNode.type, OlapTableEnum.CUSTOMWORKFACT);
                    appNifiSettingDTO.tableType = OlapTableEnum.CUSTOMWORKFACT;
                }
                appNifiSettingDTO.appId = String.valueOf(nifiNode.appId);
                appNifiSettingDTO.appPid = String.valueOf(nifiNode.groupId);
                appNifiSettingDTO.tableId = Integer.valueOf(nifiNode.tableId);
                appNifiSettingDTO.type = nifiNode.type.getValue();
                //appNifiSettingDTOS.add(appNifiSettingDTO);
            } else if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKDATAMODELDIMENSIONKPL) ||
                    Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKDATAMODELFACTKPL)) {
                BuildNifiFlowDTO buildNifiFlowDTO = new BuildNifiFlowDTO();
                OlapPO olapPO = new OlapPO();
                buildNifiFlowDTO.userId = nifiCustomWorkListDTO.userId;
                HashMap<String, Object> conditionHashMap = new HashMap<>();
                conditionHashMap.put("del_flag", 1);
                conditionHashMap.put("table_id", nifiNode.tableId);
                if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKDATAMODELDIMENSIONKPL)) {
                    conditionHashMap.put("type", 1);
                } else {
                    conditionHashMap.put("type", 0);
                }
                List<OlapPO> olapPOS = olapMapper.selectByMap(conditionHashMap);
                if (olapPOS.size() > 0) {
                    olapPO = olapPOS.get(0);
                } else {
                    log.error("未找到对应指标表" + nifiNode.type + "表id" + nifiNode.tableId);
                }
                log.info("表类别:" + nifiNode.type + "表id:" + nifiNode.tableId + "表信息" + olapPO);
                TableNifiSettingPO one1 = tableNifiSettingService.query().eq("table_access_id", olapPO.id).eq("type", OlapTableEnum.KPI.getValue()).eq("del_flag", 1).one();
                buildNifiFlowDTO.id = Long.valueOf(one1.tableAccessId);
                buildNifiFlowDTO.type = nifiNode.tableType;
                buildNifiFlowDTO.appId = Long.valueOf(one1.appId);
                buildNifiFlowDTO.dataClassifyEnum = DataClassifyEnum.DATAMODELKPL;
                buildNifiFlowDTO.synchronousTypeEnum = SynchronousTypeEnum.PGTODORIS;
                buildNifiFlowDTO.tableName = one1.tableName;
                buildNifiFlowDTO.selectSql = one1.selectSql;
                buildNifiFlowDTO.groupComponentId = groupId;
                buildNifiFlowDTO.workflowDetailId = String.valueOf(nifiNode.workflowDetailId);
                buildNifiFlowDTO.nifiCustomWorkflowId = nifiCustomWorkListDTO.nifiCustomWorkflowId;
                buildNifiFlowDTO.groupStructureId = groupStructure;
                //publishTaskController.publishBuildNifiFlowTask(buildNifiFlowDTO);
                buildNifiTaskListener.msg(JSON.toJSONString(buildNifiFlowDTO), null, null);
                appNifiSettingDTO.appId = String.valueOf(nifiNode.appId);
                appNifiSettingDTO.appPid = String.valueOf(nifiNode.groupId);
                appNifiSettingDTO.tableId = Integer.valueOf(nifiNode.tableId);
                //appNifiSettingDTO.tableType = OlapTableEnum.CUSTOMWORKKPI;
                appNifiSettingDTO.type = nifiNode.type.getValue();
                //appNifiSettingDTOS.add(appNifiSettingDTO);
            } else if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT)) {
                //调度组件,在对应组下创建.
                BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
                querySqlDto.name = nifiNode.nifiCustomWorkflowName;
                querySqlDto.details = nifiNode.nifiCustomWorkflowName;
                querySqlDto.groupId = groupId;
                querySqlDto.querySql = "select count(1) as nums from tb_etl_Incremental";
                //配置库
                NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName()).one();
                if (nifiConfigPO != null) {
                    querySqlDto.dbConnectionId = nifiConfigPO.componentId;
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, "未创建配置库连接池");
                }
                querySqlDto.scheduleExpression = nifiNode.scheduleExpression;
                querySqlDto.scheduleType = nifiNode.scheduleType;
                querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
                BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
                if (!querySqlRes.success) {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, querySqlRes.msg);
                }
                //保存组件信息
                NifiSchedulingComponentPO nifiSchedulingComponentPO = new NifiSchedulingComponentPO();
                nifiSchedulingComponentPO.componentId = querySqlRes.data.getId();
                nifiSchedulingComponentPO.groupComponentId = groupId;
                nifiSchedulingComponentPO.name = nifiNode.nifiCustomWorkflowName;
                nifiSchedulingComponentPO.nifiCustomWorkflowDetailId = String.valueOf(nifiNode.nifiCustomWorkflowId);
                nifiSchedulingComponent.save(nifiSchedulingComponentPO);
            }


        }


        return appNifiSettingDTOS;
    }

    /*
     *port,漏斗之间的连线
     */
    private void createNotifyProcessor(String groupStructure, Map<Map, Map> structure, int flag) {
        PortRequestParamDTO portRequestParamDTO = new PortRequestParamDTO();
        Iterator<Map.Entry<Map, Map>> entries = structure.entrySet().iterator();
        while (entries.hasNext()) {
            //(父,子)
            Map.Entry<Map, Map> entry = entries.next();
            log.info("父对象 = " + entry.getKey() + ", 子对象 = " + entry.getValue());
            Map<String, String> fnode = entry.getKey();
            if (flag == 1) {
                Iterator<Map.Entry<String, String>> iterator = fnode.entrySet().iterator();
                Map.Entry<String, String> entry1 = iterator.next();
                portRequestParamDTO.id = entry1.getKey();
                portRequestParamDTO.flag = flag;
                createProcessorAndConnection(groupStructure, portRequestParamDTO, portRequestParamDTO.id);

            }
            Map<String, String> node = entry.getValue();
            Iterator<Map.Entry<String, String>> entries1 = node.entrySet().iterator();
            while (entries1.hasNext()) {
                Map.Entry<String, String> entry1 = entries1.next();
                log.info("父对象-id = " + entry1.getKey() + ", 父对象-名称 = " + entry1.getValue());
                portRequestParamDTO.flag = 2;
                portRequestParamDTO.pid = entry1.getKey();
                createProcessorAndConnection(groupStructure, portRequestParamDTO, portRequestParamDTO.pid);
            }
        }

    }

    public void createProcessorAndConnection(String groupStructure, PortRequestParamDTO portRequestParamDTO, String pKey) {
        try {
            String groupId = "";
            String appGroupId = "";
            TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();

            ResultEntity<NifiPortsDTO> filterData = dataFactoryClient.getFilterData(portRequestParamDTO);
            NifiPortsDTO data = filterData.data;
            List<NifiCustomWorkflowDetailDTO> inports = data.inports;
            List<NifiCustomWorkflowDetailDTO> outports = data.outports;

            //1.inport与infunnel连接,我现在有父id,可以拿到漏斗和port组件
            PipelineConfigurationPO pipelineConfigurationPO = pipelineConfiguration.query().eq("app_id", pKey).one();
            groupId = pipelineConfigurationPO.appComponentId;
            //创建控制器服务,一层一个,唯一身份标识也是一层一个
            String releaseSignalIdentifier = UUID.randomUUID().toString();
            BuildRedisConnectionPoolServiceDTO buildRedisConnectionPoolServiceDTO = new BuildRedisConnectionPoolServiceDTO();
            buildRedisConnectionPoolServiceDTO.groupId = groupStructure;
            buildRedisConnectionPoolServiceDTO.connectionString = redisHost + ":6379";
            buildRedisConnectionPoolServiceDTO.details = "RedisConnectionPoolService";
            buildRedisConnectionPoolServiceDTO.name = "RedisConnectionPoolService";
            buildRedisConnectionPoolServiceDTO.enabled = true;
            BusinessResult<ControllerServiceEntity> redisConnectionPoolService = componentsBuild.createRedisConnectionPoolService(buildRedisConnectionPoolServiceDTO);
            BuildRedisDistributedMapCacheClientServiceDTO buildRedisDistributedMapCacheClientServiceDTO = new BuildRedisDistributedMapCacheClientServiceDTO();
            buildRedisDistributedMapCacheClientServiceDTO.groupId = groupStructure;
            buildRedisDistributedMapCacheClientServiceDTO.enabled = true;
            buildRedisDistributedMapCacheClientServiceDTO.name = "RedisDistributedMapCacheClientService";
            buildRedisDistributedMapCacheClientServiceDTO.details = "RedisDistributedMapCacheClientService";
            buildRedisDistributedMapCacheClientServiceDTO.redisConnectionPool = redisConnectionPoolService.data.getId();
            BusinessResult<ControllerServiceEntity> redisDistributedMapCacheClientService = componentsBuild.createRedisDistributedMapCacheClientService(buildRedisDistributedMapCacheClientServiceDTO);
            //生成wait组件
            BuildWaitProcessorDTO buildWaitProcessorDTO = new BuildWaitProcessorDTO();
            buildWaitProcessorDTO.details = "buildWaitProcessorDTO";
            buildWaitProcessorDTO.name = "buildWaitProcessorDTO";
            buildWaitProcessorDTO.groupId = groupId;
            buildWaitProcessorDTO.distributedCacheService = redisDistributedMapCacheClientService.data.getId();
            buildWaitProcessorDTO.expirationDuration = "3 min";
            buildWaitProcessorDTO.releaseSignalIdentifier = releaseSignalIdentifier;
            buildWaitProcessorDTO.targetSignalCount = String.valueOf(outports.size());
            BusinessResult<ProcessorEntity> waitProcessor = componentsBuild.createWaitProcessor(buildWaitProcessorDTO);

            //2.infunnel与各个组连接
            for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO : inports) {
                if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.TASKGROUP.getName())) {
                    PipelineConfigurationPO pipelineConfigurationPO1 = pipelineConfiguration.query().eq("app_id", nifiCustomWorkflowDetailDTO.id).one();
                    // 连接api infunnel连接pipelineConfigurationPO1的inputport
                    buildNifiTaskListener.buildPortConnection(pipelineConfigurationPO.appComponentId, pipelineConfigurationPO1.appComponentId, pipelineConfigurationPO1.inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                            pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.inFunnelId, ConnectableDTO.TypeEnum.FUNNEL, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                } else if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.DATALAKE_TASK.getName())) {
                    // 连接api infunnel连接tb_table_nifi_setting,物理表的table_inputport,table_id
                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", nifiCustomWorkflowDetailDTO.tableId).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKPHYSICS.getValue()).one();

                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();

                    buildNifiTaskListener.buildPortConnection(pipelineConfigurationPO.appComponentId, appGroupId, tableNifiSettingPO.tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                            pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.inFunnelId, ConnectableDTO.TypeEnum.FUNNEL, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                } else if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.DW_DIMENSION_TASK.getName())) {
                    // 连接api infunnel连接tb_table_nifi_setting,维度的table_inputport,table_id
                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", nifiCustomWorkflowDetailDTO.tableId).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKDIMENSION.getValue()).one();
                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();
                    buildNifiTaskListener.buildPortConnection(pipelineConfigurationPO.appComponentId, appGroupId, tableNifiSettingPO.tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                            pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.inFunnelId, ConnectableDTO.TypeEnum.FUNNEL, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                } else if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.DW_FACT_TASK.getName())) {
                    // 连接api infunnel连接tb_table_nifi_setting,事实的table_inputport,table_id
                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", nifiCustomWorkflowDetailDTO.tableId).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKFACT.getValue()).one();
                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();
                    buildNifiTaskListener.buildPortConnection(pipelineConfigurationPO.appComponentId, appGroupId, tableNifiSettingPO.tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                            pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.inFunnelId, ConnectableDTO.TypeEnum.FUNNEL, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                } else if (nifiCustomWorkflowDetailDTO.componentType.startsWith("分析模型")) {
                    // 连接api infunnel连接tb_table_nifi_setting,指标的table_inputport,table_id
                    HashMap<String, Object> conditionHashMap = new HashMap<>();
                    List<OlapPO> olapPOS = new ArrayList<>();
                    OlapPO olapPO = new OlapPO();
                    conditionHashMap.put("del_flag", 1);
                    conditionHashMap.put("table_id", nifiCustomWorkflowDetailDTO.tableId);
                    log.info("判断值:" + Objects.equals(ChannelDataEnum.OLAP_FACT_TASK.getName(), nifiCustomWorkflowDetailDTO.componentType));
                    log.info("连接点参数:" + nifiCustomWorkflowDetailDTO);
                    if (Objects.equals(ChannelDataEnum.OLAP_FACT_TASK.getName(), nifiCustomWorkflowDetailDTO.componentType)) {
                        conditionHashMap.put("type", 0);
                        olapPOS = olapMapper.selectByMap(conditionHashMap);
                    } else {
                        conditionHashMap.put("type", 1);
                        olapPOS = olapMapper.selectByMap(conditionHashMap);
                    }
                    log.info("查询条件:" + conditionHashMap);
                    if (olapPOS != null && olapPOS.size() != 0) {
                        olapPO = olapPOS.get(0);
                    } else {
                        log.error("未找到对应指标表" + nifiCustomWorkflowDetailDTO.componentType + "表id" + nifiCustomWorkflowDetailDTO.tableId);
                    }
                    if (Objects.equals(ChannelDataEnum.OLAP_FACT_TASK.getName(), nifiCustomWorkflowDetailDTO.componentType)) {
                        tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", olapPO.id)
                                .eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKFACTKPI).one();

                    } else {
                        tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", olapPO.id)
                                .eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKDIMENSIONKPI).one();
                    }
                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();
                    buildNifiTaskListener.buildPortConnection(pipelineConfigurationPO.appComponentId, appGroupId, tableNifiSettingPO.tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                            pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.inFunnelId, ConnectableDTO.TypeEnum.FUNNEL, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                }
            }

            for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO : outports) {
                //创建notify组件,一个nifiCustomWorkflowDetailDTO一个
                BuildNotifyProcessorDTO buildNotifyProcessorDTO = new BuildNotifyProcessorDTO();
                buildNotifyProcessorDTO.groupId = groupId;
                buildNotifyProcessorDTO.name = "NotifyProcessor";
                buildNotifyProcessorDTO.details = "NotifyProcessor";
                buildNotifyProcessorDTO.distributedCacheService = redisDistributedMapCacheClientService.data.getId();
                buildNotifyProcessorDTO.releaseSignalIdentifier = releaseSignalIdentifier;
                BusinessResult<ProcessorEntity> notifyProcessor = componentsBuild.createNotifyProcessor(buildNotifyProcessorDTO);

                if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.TASKGROUP.getName())) {
                    PipelineConfigurationPO pipelineConfigurationPO1 = pipelineConfiguration.query().eq("app_id", nifiCustomWorkflowDetailDTO.id).one();
                    // 找到子组,在其下,创建控制器服务与notify组件,连接pipelineConfigurationPO1的outputport和notify组件
                    buildNifiTaskListener.buildPortConnection(groupId, groupId, notifyProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                            pipelineConfigurationPO1.appComponentId, pipelineConfigurationPO1.outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);


                } else if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.DATALAKE_TASK.getName())) {
                    // 连接tb_table_nifi_setting,物理表的table_inputport,table_id和api outfunnel
                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", nifiCustomWorkflowDetailDTO.tableId).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKPHYSICS.getValue()).one();
                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();
                    buildNifiTaskListener.buildPortConnection(groupId, groupId, notifyProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                            appGroupId, tableNifiSettingPO.tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);


                } else if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.DW_DIMENSION_TASK.getName())) {
                    // 连接tb_table_nifi_setting,维度的table_inputport,table_id和api outfunnel
                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", nifiCustomWorkflowDetailDTO.tableId).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKDIMENSION.getValue()).one();
                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();
                    buildNifiTaskListener.buildPortConnection(groupId, groupId, notifyProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                            appGroupId, tableNifiSettingPO.tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                } else if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.DW_FACT_TASK.getName())) {
                    // 连接tb_table_nifi_setting,事实的table_inputport,table_id和api outfunnel
                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", nifiCustomWorkflowDetailDTO.tableId).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKFACT.getValue()).one();
                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();
                    buildNifiTaskListener.buildPortConnection(groupId, groupId, notifyProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                            appGroupId, tableNifiSettingPO.tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                } else if (nifiCustomWorkflowDetailDTO.componentType.startsWith("分析模型")) {
                    // 连接tb_table_nifi_setting,指标的table_inputport,table_id和api outfunnel
                    OlapPO olapPO = new OlapPO();
                    if (Objects.equals(nifiCustomWorkflowDetailDTO.componentType, ChannelDataEnum.OLAP_DIMENSION_TASK.getName())) {
                        olapPO = getOlapPO(1, Integer.parseInt(nifiCustomWorkflowDetailDTO.tableId));
                        tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", olapPO.id).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKDIMENSIONKPI.getValue()).one();
                    } else {
                        olapPO = getOlapPO(0, Integer.parseInt(nifiCustomWorkflowDetailDTO.tableId));
                        tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", olapPO.id).eq("nifi_custom_workflow_detail_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMWORKFACTKPI.getValue()).one();
                    }
                    appGroupId = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId).getComponent().getParentGroupId();
                    buildNifiTaskListener.buildPortConnection(groupId, groupId, notifyProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                            appGroupId, tableNifiSettingPO.tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

                }
                buildNifiTaskListener.buildPortConnection(groupId, pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.outFunnelId, ConnectableDTO.TypeEnum.FUNNEL,
                        groupId, notifyProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR, 3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);

            }
            // pipelineConfigurationPO.inport-pipelineConfigurationPO.infunnel      outfunnel-wait  wait-wait  wait-outport
            buildNifiTaskListener.buildPortConnection(groupId, groupId, pipelineConfigurationPO.inFunnelId, ConnectableDTO.TypeEnum.FUNNEL,
                    groupId, pipelineConfigurationPO.inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);
            buildNifiTaskListener.buildPortConnection(groupId, groupId, waitProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                    groupId, pipelineConfigurationPO.outFunnelId, ConnectableDTO.TypeEnum.FUNNEL, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);
            buildNifiTaskListener.buildPortConnection(groupId, groupId, waitProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                    groupId, waitProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR, 4, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);
            buildNifiTaskListener.buildPortConnection(groupId, groupId, pipelineConfigurationPO.outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                    groupId, waitProcessor.data.getId(), ConnectableDTO.TypeEnum.PROCESSOR, 3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    /*
     * 创建节点之间的连接线,并保存连接线信息
     * */
    public void createConnectingLine(NifiCustomWorkListDTO nifiCustomWorkListDTO) {
        try {
            //1.开始连线,需要分类,组件与组件,组件与组,组与组,每一层
            List<NifiCustomWorkDTO> nifiCustomWorkDTOS = nifiCustomWorkListDTO.nifiCustomWorkDTOS;
            //输入节点
            AppNifiSettingPO inputDuct = new AppNifiSettingDTO();
            //本节点
            AppNifiSettingPO thisNode = new AppNifiSettingDTO();
            //输出节点
            AppNifiSettingPO outputDuct = new AppNifiSettingDTO();
            Map<Map, Map> structure = nifiCustomWorkListDTO.structure;
            for (NifiCustomWorkDTO nifiCustomWorkDTO : nifiCustomWorkDTOS) {
                //本节点
                BuildNifiCustomWorkFlowDTO nifiNode = nifiCustomWorkDTO.NifiNode;
                thisNode = appNifiSettingService.query().eq("app_id", nifiNode.appId).eq("type", nifiNode.type.getValue()).eq("nifi_custom_workflow_id", nifiCustomWorkListDTO.nifiCustomWorkflowId).eq("del_flag", 1).one();
                //输入节点
                List<BuildNifiCustomWorkFlowDTO> inputDucts = nifiCustomWorkDTO.inputDucts;
                //输出节点
                List<BuildNifiCustomWorkFlowDTO> outputDucts = nifiCustomWorkDTO.outputDucts;
                //连线连接的是port,调度组件特殊,直接连接它本身,
                //先判断本节点的类型,再判断与之连接的节点的类型
                if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT)) {
                    NifiSchedulingComponentPO nifiSchedulingComponentPO = nifiSchedulingComponent.query().eq("nifi_custom_workflow_detail_id", nifiNode.nifiCustomWorkflowId).eq("del_flag", 1).one();
                    String componentId = nifiSchedulingComponentPO.componentId;
                    ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor(componentId);
                    String parentGroupId = processor.getComponent().getParentGroupId();
                    if (outputDucts != null && outputDucts.size() != 0) {
                        for (BuildNifiCustomWorkFlowDTO buildNifiCustomWorkFlowDTO : outputDucts) {
                            //调度组件与层级组连接
                            if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                                //端点
                                PipelineConfigurationPO pipelineConfigurationPO = pipelineConfiguration.query().eq("app_id", buildNifiCustomWorkFlowDTO.appId).eq("del_flag", 1).one();
                                String inputPortId = pipelineConfigurationPO.inputPortId;
                                // 调用连接api
                                buildNifiTaskListener.buildPortConnection2(parentGroupId, pipelineConfigurationPO.appComponentId, inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        parentGroupId, componentId, ConnectableDTO.TypeEnum.PROCESSOR, 3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);

                            } else {
                                //调度组件与任务流连接
                                TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
                                OlapPO olapPO = new OlapPO();
                                if (Objects.equals(buildNifiCustomWorkFlowDTO.tableType.getValue(), 4)) {
                                    olapPO = getOlapPO(0, Integer.parseInt(buildNifiCustomWorkFlowDTO.tableId));
                                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", olapPO.id).eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.workflowDetailId).eq("type", buildNifiCustomWorkFlowDTO.tableType.getValue()).eq("del_flag", 1).one();
                                } else if (Objects.equals(buildNifiCustomWorkFlowDTO.tableType.getValue(), 8)) {
                                    olapPO = getOlapPO(1, Integer.parseInt(buildNifiCustomWorkFlowDTO.tableId));
                                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", olapPO.id).eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.workflowDetailId).eq("type", buildNifiCustomWorkFlowDTO.tableType.getValue()).eq("del_flag", 1).one();
                                } else {
                                    tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", buildNifiCustomWorkFlowDTO.tableId).eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.workflowDetailId).eq("type", buildNifiCustomWorkFlowDTO.tableType.getValue()).eq("del_flag", 1).one();
                                }
                                log.info("连接对象参数:" + tableNifiSettingPO);

                                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId);
                                //流程组port组件父id
                                String parentGroupId1 = processGroup.getComponent().getParentGroupId();
                                String tableInputPortId = tableNifiSettingPO.tableInputPortId;
                                // 调用连接api
                                buildNifiTaskListener.buildPortConnection2(parentGroupId, parentGroupId1, tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        parentGroupId, componentId, ConnectableDTO.TypeEnum.PROCESSOR, 3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);

                            }
                        }
                    }
                    //层级组
                } else if (Objects.equals(nifiNode.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                    //要判断本端点是什么成分,如果是层级组,就找tb_pipeline_configuration,流程组就找tb_table_nifi_setting,如果是本组最后一批接入的,就连接本组的outfunnel
                    PipelineConfigurationPO pipelineConfigurationPO = pipelineConfiguration.query().eq("app_id", nifiNode.appId).eq("del_flag", 1).one();
                    if (inputDucts != null && inputDucts.size() != 0) {
                        for (BuildNifiCustomWorkFlowDTO buildNifiCustomWorkFlowDTO : inputDucts) {
                            if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                                PipelineConfigurationPO pipelineConfigurationPO1 = pipelineConfiguration.query().eq("app_id", buildNifiCustomWorkFlowDTO.appId).eq("del_flag", 1).one();
                                String outputPortId = pipelineConfigurationPO1.outputPortId;
                                String inputPortId = pipelineConfigurationPO.inputPortId;
                                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(pipelineConfigurationPO1.appComponentId);
                                String pGroupId = processGroup.getComponent().getParentGroupId();
                                //   调用连接api
                                buildNifiTaskListener.buildPortConnection2(pGroupId, pipelineConfigurationPO.appComponentId, inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        pipelineConfigurationPO1.appComponentId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);

                            } else if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT)) {
                                NifiSchedulingComponentPO one = nifiSchedulingComponent.query().eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.nifiCustomWorkflowId).eq("del_flag", 1).one();

                                /*buildNifiTaskListener.buildPortConnection2(one.groupComponentId, pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        one.groupComponentId, one.componentId, ConnectableDTO.TypeEnum.PROCESSOR, 3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION,ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);
*/
                            } else {
                                TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", buildNifiCustomWorkFlowDTO.tableId).eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.workflowDetailId).eq("type", buildNifiCustomWorkFlowDTO.tableType.getValue()).eq("del_flag", 1).one();
                                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId);
                                String tableOutputPortId = tableNifiSettingPO.tableOutputPortId;
                                String outputPortId = pipelineConfigurationPO.outputPortId;
                                //  调用连接api
                                buildNifiTaskListener.buildPortConnection2(pipelineConfigurationPO.appComponentId, processGroup.getId(), tableOutputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        pipelineConfigurationPO.appComponentId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);


                            }
                        }
                    }
                    if (outputDucts != null && outputDucts.size() != 0) {
                        for (BuildNifiCustomWorkFlowDTO buildNifiCustomWorkFlowDTO : outputDucts) {
                            if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                                String outputPortId = pipelineConfigurationPO.outputPortId;
                                PipelineConfigurationPO pipelineConfigurationPO1 = pipelineConfiguration.query().eq("app_id", buildNifiCustomWorkFlowDTO.appId).eq("del_flag", 1).one();
                                String inputPortId = pipelineConfigurationPO1.inputPortId;
                                //  调用连接api
                                buildNifiTaskListener.buildPortConnection2(pipelineConfigurationPO.appComponentId, pipelineConfigurationPO1.appComponentId, inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        pipelineConfigurationPO.appComponentId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);

                            } else {
                                String outputPortId = pipelineConfigurationPO.outputPortId;
                                TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", buildNifiCustomWorkFlowDTO.tableId).eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.workflowDetailId).eq("type", buildNifiCustomWorkFlowDTO.tableType.getValue()).eq("del_flag", 1).one();
                                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId);
                                String tableInputPortId = tableNifiSettingPO.tableInputPortId;
                                ProcessGroupEntity processGroup1 = NifiHelper.getProcessGroupsApi().getProcessGroup(pipelineConfigurationPO.appComponentId);
                                String pGroupId = processGroup1.getComponent().getParentGroupId();
                                buildNifiTaskListener.buildPortConnection2(pGroupId, processGroup.getComponent().getParentGroupId(), tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        pipelineConfigurationPO.appComponentId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);
                            }
                        }
                    }
                } else {
                    String tableId = "";
                    OlapPO olapPO = new OlapPO();
                    //如果是维度表
                    if (Objects.equals(nifiNode.tableType, OlapTableEnum.CUSTOMWORKDIMENSIONKPI)) {
                        olapPO = getOlapPO(1, Integer.parseInt(nifiNode.tableId));
                        tableId = String.valueOf(olapPO.id);
                        //如果是事实表
                    } else if (Objects.equals(nifiNode.tableType, OlapTableEnum.CUSTOMWORKFACTKPI)) {
                        olapPO = getOlapPO(0, Integer.parseInt(nifiNode.tableId));
                        tableId = String.valueOf(olapPO.id);
                    } else {
                        tableId = nifiNode.tableId;
                    }
                    TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("table_access_id", tableId)
                            .eq("nifi_custom_workflow_detail_id", nifiNode.workflowDetailId).eq("type", nifiNode.tableType.getValue()).eq("del_flag", 1).one();
                    ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId);
                    if (inputDucts != null && inputDucts.size() != 0) {
                        for (BuildNifiCustomWorkFlowDTO buildNifiCustomWorkFlowDTO : inputDucts) {
                            if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                                PipelineConfigurationPO pipelineConfigurationPO1 = pipelineConfiguration.query().eq("app_id", buildNifiCustomWorkFlowDTO.appId).eq("del_flag", 1).one();
                                String outputPortId = pipelineConfigurationPO1.outputPortId;
                                //  调用连接api
                                buildNifiTaskListener.buildPortConnection2(pipelineConfigurationPO1.appComponentId, processGroup.getId(), tableNifiSettingPO.tableOutputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        pipelineConfigurationPO1.appComponentId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);


                            } else if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT)) {
                                NifiSchedulingComponentPO one = nifiSchedulingComponent.query().eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.nifiCustomWorkflowId).eq("del_flag", 1).one();

                               /* buildNifiTaskListener.buildPortConnection(one.groupComponentId, pipelineConfigurationPO.appComponentId, pipelineConfigurationPO.inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        one.groupComponentId, one.componentId, ConnectableDTO.TypeEnum.PROCESSOR, 3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);
*/
                            } else {
                                String tableId1 = "";
                                if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKDATAMODELFACTKPL)) {
                                    OlapPO olapPO1 = getOlapPO(0, Integer.parseInt(buildNifiCustomWorkFlowDTO.tableId));
                                    tableId1 = String.valueOf(olapPO1.id);
                                } else if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKDATAMODELDIMENSIONKPL)) {
                                    OlapPO olapPO1 = getOlapPO(1, Integer.parseInt(buildNifiCustomWorkFlowDTO.tableId));
                                    tableId1 = String.valueOf(olapPO1.id);
                                } else {
                                    tableId1 = buildNifiCustomWorkFlowDTO.tableId;
                                }
                                TableNifiSettingPO tableNifiSettingPO1 = tableNifiSettingService.query().eq("table_access_id", tableId1).eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.workflowDetailId).eq("type", buildNifiCustomWorkFlowDTO.tableType.getValue()).eq("del_flag", 1).one();
                                ProcessGroupEntity processGroup1 = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO1.tableComponentId);
                                String tableOutputPortId = tableNifiSettingPO1.tableOutputPortId;
                                //  调用连接api
                                buildNifiTaskListener.buildPortConnection2(processGroup1.getId(), processGroup.getId(), tableNifiSettingPO.tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        processGroup1.getId(), tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);


                            }
                        }
                    }
                    if (outputDucts != null && outputDucts.size() != 0) {
                        for (BuildNifiCustomWorkFlowDTO buildNifiCustomWorkFlowDTO : outputDucts) {
                            if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKSTRUCTURE)) {
                                PipelineConfigurationPO pipelineConfigurationPO1 = pipelineConfiguration.query().eq("app_id", buildNifiCustomWorkFlowDTO.appId).eq("del_flag", 1).one();
                                String inputPortId = pipelineConfigurationPO1.inputPortId;
                                //  调用连接api
                                buildNifiTaskListener.buildPortConnection2(processGroup.getId(), pipelineConfigurationPO1.appComponentId, pipelineConfigurationPO1.outputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        processGroup.getId(), inputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);


                            } else {
                                String tableId1 = "";
                                if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKDATAMODELFACTKPL)) {
                                    OlapPO olapPO1 = getOlapPO(0, Integer.parseInt(buildNifiCustomWorkFlowDTO.tableId));
                                    tableId1 = String.valueOf(olapPO1.id);
                                } else if (Objects.equals(buildNifiCustomWorkFlowDTO.type, DataClassifyEnum.CUSTOMWORKDATAMODELDIMENSIONKPL)) {
                                    OlapPO olapPO1 = getOlapPO(1, Integer.parseInt(buildNifiCustomWorkFlowDTO.tableId));
                                    tableId1 = String.valueOf(olapPO1.id);
                                } else {
                                    tableId1 = buildNifiCustomWorkFlowDTO.tableId;
                                }
                                TableNifiSettingPO tableNifiSettingPO1 = tableNifiSettingService.query().eq("table_access_id", tableId1).eq("nifi_custom_workflow_detail_id", buildNifiCustomWorkFlowDTO.workflowDetailId).eq("type", buildNifiCustomWorkFlowDTO.tableType.getValue()).eq("del_flag", 1).one();

                                ProcessGroupEntity processGroup1 = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO1.tableComponentId);
                                String parentGroupId = processGroup1.getComponent().getParentGroupId();
                                String tableInputPortId = tableNifiSettingPO1.tableInputPortId;
                                ProcessGroupEntity processGroup2 = NifiHelper.getProcessGroupsApi().getProcessGroup(parentGroupId);
                                String parentGroupId1 = processGroup2.getComponent().getParentGroupId();
                                PortEntity outputPort = NifiHelper.getOutputPortsApi().getOutputPort(tableNifiSettingPO.tableOutputPortId);
                                String parentGroupId2 = outputPort.getComponent().getParentGroupId();
                                //  调用连接api
                                buildNifiTaskListener.buildPortConnection2(parentGroupId1, parentGroupId, tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                                        parentGroupId2, tableNifiSettingPO.tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT, 0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);

                            }
                        }
                    }
                }
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public OlapPO getOlapPO(int type, int tableId) {
        HashMap<String, Object> conditionHashMap = new HashMap<>();
        List<OlapPO> olapPOS = new ArrayList<>();
        OlapPO olapPO = new OlapPO();
        conditionHashMap.put("del_flag", 1);
        conditionHashMap.put("table_id", tableId);
        conditionHashMap.put("type", type);
        olapPOS = olapMapper.selectByMap(conditionHashMap);
        if (olapPOS != null && olapPOS.size() != 0) {
            olapPO = olapPOS.get(0);
        } else {
            log.error("未找到对应指标表");
        }
        return olapPO;
    }

}
