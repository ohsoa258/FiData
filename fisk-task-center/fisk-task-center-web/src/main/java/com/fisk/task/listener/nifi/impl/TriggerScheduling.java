package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.task.dto.nifi.BuildConvertToJsonProcessorDTO;
import com.fisk.task.dto.nifi.BuildExecuteSqlProcessorDTO;
import com.fisk.task.dto.nifi.BuildProcessGroupDTO;
import com.fisk.task.dto.nifi.BuildPublishKafkaProcessorDTO;
import com.fisk.task.dto.task.UnifiedControlDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.listener.nifi.ITriggerScheduling;
import com.fisk.task.po.NifiConfigPO;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.impl.NifiConfigServiceImpl;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.NifiPositionHelper;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author cfk
 */
@Component
@Slf4j
public class TriggerScheduling implements ITriggerScheduling {
    @Resource
    NifiConfigServiceImpl nifiConfigService;
    @Resource
    INiFiHelper componentsBuild;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Value("${spring.kafka.producer.bootstrap-servers}")
    public String KafkaBrokers;


    @Override
    public ResultEnum unifiedControl(String data, Acknowledgment acknowledgment) {
        try {

            UnifiedControlDTO unifiedControlDTO = JSON.parseObject(data, UnifiedControlDTO.class);
            int id = unifiedControlDTO.id;
            //流程type
            int unifiedcontrol = unifiedControlDTO.type.getValue();
            String topic = unifiedControlDTO.topic;
            String dzGroupId = "";
            //1.创建大组
            NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName()).one();
            if (nifiConfigPO != null) {
                dzGroupId = nifiConfigPO.componentId;
            } else {
                BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
                buildProcessGroupDTO.name = ComponentIdTypeEnum.TRIGGERSCHEDULING_NIFI_FLOW_GROUP_ID.getName();
                buildProcessGroupDTO.details = ComponentIdTypeEnum.TRIGGERSCHEDULING_NIFI_FLOW_GROUP_ID.getName();
                int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
                buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
                BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
                if (processGroupEntityBusinessResult.success) {
                    dzGroupId = processGroupEntityBusinessResult.data.getId();
                    NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                    nifiConfigPO1.componentId = dzGroupId;
                    nifiConfigPO1.componentKey = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                    nifiConfigService.save(nifiConfigPO1);
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processGroupEntityBusinessResult.msg);
                }
            }
            //2.创建小组
            TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.getByTableId(id, unifiedcontrol);
            if (tableNifiSettingPO != null) {
                List<ProcessorEntity> entities = new ArrayList<>();
                ProcessorEntity dispatchComponentProcessor = NifiHelper.getProcessorsApi().getProcessor(tableNifiSettingPO.dispatchComponentId);
                ProcessorEntity publishKafkaProcessor = NifiHelper.getProcessorsApi().getProcessor(tableNifiSettingPO.publishKafkaProcessorId);
                entities.add(publishKafkaProcessor);
                entities.add(dispatchComponentProcessor);
                componentsBuild.stopProcessor(tableNifiSettingPO.tableComponentId, entities);
                componentsBuild.emptyNifiConnectionQueue(tableNifiSettingPO.tableComponentId);
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(tableNifiSettingPO.tableComponentId);
                NifiHelper.getProcessGroupsApi().removeProcessGroup(processGroup.getId(), String.valueOf(processGroup.getRevision().getVersion()), null, null);
                tableNifiSettingService.removeById(tableNifiSettingPO.id);
            }
            //如果是禁用或者删除,就不会重新创建
            if (!unifiedControlDTO.deleted) {
                //配置库
                NifiConfigPO nifiConfigPO1 = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName()).one();
                if (nifiConfigPO1 != null) {
                    log.info("配置库连接成功");
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, "未创建配置库连接池");
                }
                BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
                dto.name = "统一调度";
                dto.details = "统一调度";
                dto.groupId = dzGroupId;
                //根据组个数，定义坐标
                int count = componentsBuild.getGroupCount(dzGroupId);
                dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
                //创建组件
                BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
                if (res.success) {
                    ProcessGroupEntity data1 = res.data;
                    List<ProcessorEntity> entityList = new ArrayList<>();
                    ProcessorEntity processorEntity = queryDispatchProcessor(data1.getId(), nifiConfigPO1.componentId, unifiedControlDTO);
                    ProcessorEntity convertDataToJson = convertDataToJson(data1.getId());
                    ProcessorEntity publishKafkaProcessor = createPublishKafkaProcessor(data1.getId(), topic);
                    entityList.add(processorEntity);
                    entityList.add(convertDataToJson);
                    entityList.add(publishKafkaProcessor);
                    componentsBuild.buildConnectProcessors(data1.getId(), processorEntity.getId(), convertDataToJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    componentsBuild.buildConnectProcessors(data1.getId(), convertDataToJson.getId(), publishKafkaProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    componentsBuild.enabledProcessor(data1.getId(), entityList);
                    TableNifiSettingPO tableNifiSetting = new TableNifiSettingPO();
                    tableNifiSetting.tableAccessId = id;
                    tableNifiSetting.type = unifiedcontrol;
                    tableNifiSetting.dispatchComponentId = processorEntity.getId();
                    tableNifiSetting.convertDataToJsonProcessorId = convertDataToJson.getId();
                    tableNifiSetting.publishKafkaProcessorId = publishKafkaProcessor.getId();
                    tableNifiSetting.tableComponentId = data1.getId();
                    tableNifiSettingService.save(tableNifiSetting);
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
            }
            return ResultEnum.SUCCESS;
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        } finally {
            acknowledgment.acknowledge();
        }
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param groupId     组id
     * @param cfgDbPoolId 增量配置库连接池id
     * @return 组件对象
     */
    private ProcessorEntity queryDispatchProcessor(String groupId, String cfgDbPoolId, UnifiedControlDTO unifiedControlDTO) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "queryDispatchProcessor";
        querySqlDto.details = "queryDispatchProcessor";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = "select " + unifiedControlDTO.id + " as id, " + unifiedControlDTO.type + " as templateModulesType," + unifiedControlDTO.userId + " as userId";
        querySqlDto.dbConnectionId = cfgDbPoolId;
        querySqlDto.scheduleExpression = unifiedControlDTO.scheduleExpression;
        querySqlDto.scheduleType = unifiedControlDTO.scheduleType;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        return querySqlRes.data;
    }

    /**
     * 执行sql convertDataToJson
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity convertDataToJson(String groupId) {
        BuildConvertToJsonProcessorDTO toJsonDto = new BuildConvertToJsonProcessorDTO();
        toJsonDto.name = "Convert Data To Json";
        toJsonDto.details = "query_phase";
        toJsonDto.groupId = groupId;
        toJsonDto.positionDTO = NifiPositionHelper.buildXYPositionDTO(1, 1);
        BusinessResult<ProcessorEntity> toJsonRes = componentsBuild.buildConvertToJsonProcess(toJsonDto);
        return toJsonRes.data;
    }

    /**
     * createPublishKafkaProcessor
     *
     * @param groupId 组id
     * @return 组件对象
     */
    public ProcessorEntity createPublishKafkaProcessor(String groupId, String topic) {
        BuildPublishKafkaProcessorDTO buildPublishKafkaProcessorDTO = new BuildPublishKafkaProcessorDTO();
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildPublishKafkaProcessorDTO.KafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        buildPublishKafkaProcessorDTO.KafkaKey = "${uuid}";
        buildPublishKafkaProcessorDTO.groupId = groupId;
        buildPublishKafkaProcessorDTO.name = "PublishKafka";
        buildPublishKafkaProcessorDTO.details = "PublishKafka";
        buildPublishKafkaProcessorDTO.UseTransactions = "false";
        buildPublishKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(2);
        buildPublishKafkaProcessorDTO.TopicName = topic;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildPublishKafkaProcessor(buildPublishKafkaProcessorDTO);
        return processorEntityBusinessResult.data;
    }
}
