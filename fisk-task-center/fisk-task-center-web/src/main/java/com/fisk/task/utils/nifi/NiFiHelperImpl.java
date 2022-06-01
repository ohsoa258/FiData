package com.fisk.task.utils.nifi;

import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.api.ProcessorsApi;
import com.davis.client.model.*;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.task.FuncNameEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.core.enums.task.nifi.ControllerServiceTypeEnum;
import com.fisk.common.core.enums.task.nifi.ProcessorTypeEnum;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.dto.daconfig.AssociatedConditionDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.nifi.FunnelDTO;
import com.fisk.task.dto.nifi.ProcessorRunStatusEntity;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.TableNifiSettingDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.po.AppNifiSettingPO;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.AppNifiSettingServiceImpl;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.impl.TableTopicImpl;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.vo.ProcessGroupsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gy
 */
@Slf4j
@Service
public class NiFiHelperImpl implements INiFiHelper {

    @Resource
    RestTemplate httpClient;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    AppNifiSettingServiceImpl appNifiSettingService;
    @Resource
    TableTopicImpl tableTopic;
    @Value("${nifi.basePath}")
    public String basePath;


    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessGroupEntity> buildProcessGroup(BuildProcessGroupDTO dto) {
        //请求实体
        ProcessGroupEntity entity = new ProcessGroupEntity();

        //group实体
        ProcessGroupDTO groupDTO = new ProcessGroupDTO();

        //group基础信息
        groupDTO.setName(dto.name);
        groupDTO.setComments(dto.details);
        groupDTO.setPosition(dto.positionDTO);

        entity.setComponent(groupDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());

        try {
            ProcessGroupEntity res = NifiHelper.getProcessGroupsApi().createProcessGroup(NifiHelper.getPid(dto.groupId), entity);
            return BusinessResult.of(true, "", res);
        } catch (ApiException e) {
            log.error("分组创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "分组创建失败，" + e.getMessage(), null);
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessGroupEntity> getProcessGroupById(String id) {
        if (StringUtils.isEmpty(id)) {
            return BusinessResult.of(false, "组id不可为空", null);
        }
        try {
            ProcessGroupEntity res = NifiHelper.getProcessGroupsApi().getProcessGroup(id);
            return BusinessResult.of(true, "", res);
        } catch (ApiException e) {
            log.error("查询分组失败，，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "查询分组失败, " + e.getMessage(), null);
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ControllerServiceEntity> buildDbControllerService(BuildDbControllerServiceDTO data) {
        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(5);
        map.put("Database Connection URL", data.conUrl);
        map.put("Database Driver Class Name", data.driverName);
        map.put("database-driver-locations", data.driverLocation);
        map.put("Database User", data.user);
        map.put("Password", data.pwd);

        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setType(ControllerServiceTypeEnum.DBCP_CONNECTION_POOL.getName());
        dto.setName(data.name);
        dto.setComments(data.details);
        dto.setProperties(map);

        entity.setPosition(data.positionDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setComponent(dto);

        try {
            ControllerServiceEntity res = NifiHelper.getProcessGroupsApi().createControllerService(data.groupId, entity);
            //是否将控制器服务打开
            if (data.enabled) {
                BusinessResult<ControllerServiceEntity> model = updateDbControllerServiceState(res);
                if (model.success) {
                    return BusinessResult.of(true, "控制器服务创建成功，并开启运行", res);
                } else {
                    return BusinessResult.of(false, model.msg, null);
                }
            }
            return BusinessResult.of(true, "控制器服务创建成功", res);
        } catch (ApiException e) {
            log.error("创建连接池失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "创建连接池失败: " + e.getMessage(), null);
        }
    }

    //创建AvroRecordSetWriter
    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ControllerServiceEntity> buildAvroRecordSetWriterService(BuildAvroRecordSetWriterServiceDTO data) {
        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(5);
        //avro-embedded
        map.put("Schema Write Strategy", "avro-embedded");
        //schema-text-property
        map.put("schema-access-strategy", "schema-text-property");
        map.put("schema-text", data.schemaArchitecture);


        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setType(ControllerServiceTypeEnum.AVRORECORDSETWRITER.getName());
        dto.setName(data.name);
        dto.setComments(data.details);
        dto.setProperties(map);

        entity.setPosition(data.positionDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setComponent(dto);

        try {
            ControllerServiceEntity res = NifiHelper.getProcessGroupsApi().createControllerService(data.groupId, entity);
            BusinessResult<ControllerServiceEntity> model = updateDbControllerServiceState(res);
            if (model.success) {
                return BusinessResult.of(true, "控制器服务创建成功，并开启运行", res);
            } else {
                return BusinessResult.of(false, model.msg, null);
            }
        } catch (ApiException e) {
            log.error("创建AvroRecordSetWriter失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "创建AvroRecordSetWriter失败: " + e.getMessage(), null);
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ControllerServiceEntity> buildCSVReaderService(BuildCSVReaderProcessorDTO data) {
        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(5);
        //avro-embedded
        //schema-text-property
        map.put("schema-access-strategy", data.schemaAccessStrategy);
        map.put("schema-text", data.schemaText);
        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setType(ControllerServiceTypeEnum.CSVREADER.getName());
        dto.setName(data.name);
        dto.setComments(data.details);
        dto.setProperties(map);
        entity.setPosition(data.positionDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setComponent(dto);
        try {
            ControllerServiceEntity res = NifiHelper.getProcessGroupsApi().createControllerService(data.groupId, entity);
            BusinessResult<ControllerServiceEntity> model = updateDbControllerServiceState(res);
            if (model.success) {
                return BusinessResult.of(true, "控制器服务创建成功，并开启运行", res);
            } else {
                return BusinessResult.of(false, model.msg, null);
            }
        } catch (ApiException e) {
            log.error("创建CSVReader失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "创建CSVReader失败: " + e.getMessage(), null);
        }
    }

    //创建AvroReader
    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ControllerServiceEntity> buildAvroReaderService(BuildAvroReaderServiceDTO data) {
        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(5);
        map.put("schema-access-strategy", "schema-text-property");
        map.put("schema-text", data.schemaText);


        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setType(ControllerServiceTypeEnum.AVROREADER.getName());
        dto.setName(data.name);
        dto.setComments(data.details);
        dto.setProperties(map);

        entity.setPosition(data.positionDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setComponent(dto);

        try {
            ControllerServiceEntity res = NifiHelper.getProcessGroupsApi().createControllerService(data.groupId, entity);
            BusinessResult<ControllerServiceEntity> model = updateDbControllerServiceState(res);
            if (model.success) {
                return BusinessResult.of(true, "控制器服务创建成功，并开启运行", res);
            } else {
                return BusinessResult.of(false, model.msg, null);
            }
        } catch (ApiException e) {
            log.error("创建AvroReader失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "创建AvroReader失败: " + e.getMessage(), null);
        }
    }


    @Override
    public BusinessResult<ProcessorEntity> buildUpdateRecord(BuildUpdateRecordDTO buildUpdateRecordDTO) {
        List<String> auto = new ArrayList<>(2);
        //流程分支，是否自动结束
        auto.add(AutoEndBranchTypeEnum.FAILURE.getName());
        auto.add(AutoEndBranchTypeEnum.ORIGINAL.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(5);
        map.put("record-reader", buildUpdateRecordDTO.recordReader);
        map.put("record-writer", buildUpdateRecordDTO.recordWriter);
        map.put("replacement-value-strategy", buildUpdateRecordDTO.replacementValueStrategy);
        if (buildUpdateRecordDTO.filedMap != null && buildUpdateRecordDTO.filedMap.size() > 0) {
            map.putAll(buildUpdateRecordDTO.filedMap);
        }
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(auto);
        config.setComments(buildUpdateRecordDTO.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(buildUpdateRecordDTO.name);
        dto.setType(ProcessorTypeEnum.UPDATERECORD.getName());
        dto.setPosition(buildUpdateRecordDTO.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(buildUpdateRecordDTO.groupId, entity, dto, config);
    }


    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public ControllerServiceEntity getDbControllerService(String id) {
        try {
            return NifiHelper.getControllerServicesApi().getControllerService(id);
        } catch (ApiException e) {
            log.error("获取控制器服务失败，id【" + id + "】，【" + e.getResponseBody() + "】: ", e);
            return null;
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ControllerServiceEntity> updateDbControllerServiceState(String id) {
        return updateDbControllerServiceState(getDbControllerService(id));
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ControllerServiceEntity> updateDbControllerServiceState(ControllerServiceEntity entity) {
        if (entity == null) {
            return BusinessResult.of(false, "控制器服务不存在", null);
        }
        if (entity.getComponent().getState() != ControllerServiceDTO.StateEnum.ENABLED) {
            entity.getComponent().setState(ControllerServiceDTO.StateEnum.ENABLED);
            try {
                entity.getComponent().getProperties().remove("Password");
                ControllerServiceEntity res = NifiHelper.getControllerServicesApi().updateControllerService(entity.getId(), entity);
                return BusinessResult.of(true, "控制器状态修改成功", res);
            } catch (ApiException e) {
                log.error("修改控制器服务失败，【" + e.getResponseBody() + "】: ", e);
                return BusinessResult.of(false, "修改控制器服务失败, ex" + e.getMessage(), null);
            }
        } else {
            return BusinessResult.of(false, "控制器服务当前正在运行，不允许修改", null);
        }
    }

    @Override
    public BusinessResult<ProcessorEntity> buildMergeContentProcess(BuildMergeContentProcessorDTO data) {
        List<String> auto = new ArrayList<>(2);
        //流程分支，是否自动结束
        auto.add(AutoEndBranchTypeEnum.FAILURE.getName());
        auto.add(AutoEndBranchTypeEnum.ORIGINAL.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(5);
        map.put("Max Bin Age", "3 sec");
        map.put("Minimum Group Size", "1 GB");
        map.put("Attribute Strategy", "Keep All Unique Attributes");

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(auto);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.MergeContent.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildReplaceTextProcess(BuildReplaceTextProcessorDTO data, List<String> auto) {

        //流程分支，是否自动结束
        auto.add(AutoEndBranchTypeEnum.FAILURE.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(1);
        map.put("Replacement Value", data.replacementValue);
        map.put("Evaluation Mode", data.evaluationMode);

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(auto);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.ReplaceText.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildPublishMqProcess(BuildPublishMqProcessorDTO data) {
        List<String> auto = new ArrayList<>(2);
        //流程分支，是否自动结束
        auto.add(AutoEndBranchTypeEnum.FAILURE.getName());
        auto.add(AutoEndBranchTypeEnum.SUCCESS.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(7);
        map.put("Exchange Name", data.exchange);
        map.put("Routing Key", data.route);
        map.put("Host Name", data.host);
        map.put("Port", data.port);
        map.put("Virtual Host", data.vhost);
        map.put("User Name", data.user);
        map.put("Password", data.pwd);

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(auto);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.PublishAMQP.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildExecuteSqlProcess(BuildExecuteSqlProcessorDTO data, List<String> autoEnd) {
        //流程分支，是否自动结束
        autoEnd.add(AutoEndBranchTypeEnum.FAILURE.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(5);
        map.put("Database Connection Pooling Service", data.dbConnectionId);
        map.put("sql-pre-query", data.preSql);
        map.put("SQL select query", data.querySql);
        map.put("sql-post-query", data.postSql);
        map.put("esql-max-rows", data.MaxRowsPerFlowFile);
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        if (data.scheduleType != null) {
            config.setSchedulingStrategy(data.scheduleType.getName());
        }
        if (StringUtils.isNotEmpty(data.scheduleExpression)) {
            if (Objects.equals(data.scheduleType.getName(), SchedulingStrategyTypeEnum.TIMER.getName())) {
                config.setSchedulingPeriod(data.scheduleExpression + " sec");
            } else {
                config.setSchedulingPeriod(data.scheduleExpression);
            }
        }
        config.setProperties(map);
        config.setAutoTerminatedRelationships(autoEnd);
        config.setComments(data.details);
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.ExecuteSQL.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildInvokeHTTPProcessor(BuildInvokeHttpProcessorDTO data, List<String> autoEnd) {
        //流程分支，是否自动结束
        autoEnd.add("Failure");
        autoEnd.add("No Retry");
        autoEnd.add("Original");
        autoEnd.add("Retry");
        //组件属性
        Map<String, String> map = new HashMap<>(5);
        map.put("Attributes to Send", data.attributesToSend);
        map.put("Content-Type", data.contentType);
        map.put("HTTP Method", data.httpMethod);
        map.put("Remote URL", data.remoteUrl);
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(autoEnd);
        config.setComments(data.details);
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.INVOKEHTTP.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildGenerateFlowFileProcessor(BuildGenerateFlowFileProcessorDTO data, List<String> autoEnd) {
        //流程分支，是否自动结束
        autoEnd.add(AutoEndBranchTypeEnum.FAILURE.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(5);
        map.put("generate-ff-custom-text", data.generateCustomText);
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(autoEnd);
        config.setComments(data.details);
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.GENERATEFLOWFILE.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildConsumeKafkaProcessor(BuildConsumeKafkaProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add(AutoEndBranchTypeEnum.FAILURE.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(5);
        map.put("bootstrap.servers", data.kafkaBrokers);
        map.put("topic", data.topicNames);
        map.put("group.id", data.GroupId);
        map.put("honor-transactions", String.valueOf(data.honorTransactions));
        map.put("auto.offset.reset", "earliest");
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(autoEnd);
        config.setComments(data.details);
        config.setSchedulingPeriod("1 sec");
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.ConsumeKafka.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildPublishKafkaProcessor(BuildPublishKafkaProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoEnd.add(AutoEndBranchTypeEnum.SUCCESS.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(5);
        map.put("bootstrap.servers", data.KafkaBrokers);
        map.put("kafka-key", data.KafkaKey);
        map.put("topic", data.TopicName);
        map.put("use-transactions", data.UseTransactions);

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setProperties(map);
        config.setAutoTerminatedRelationships(autoEnd);
        config.setComments(data.details);
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.PublishKafka.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildConvertToJsonProcess(BuildConvertToJsonProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.ConvertAvroToJSON.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildConvertJsonToSqlProcess(BuildConvertJsonToSqlProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.ORIGINAL.getName());

        Map<String, String> map = new HashMap<>(3);
        map.put("Table Name", data.tableName);
        map.put("Statement Type", data.sqlType.getName());
        map.put("JDBC Connection Pool", data.dbConnectionId);


        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.ConvertJSONToSQL.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildPutSqlProcess(BuildPutSqlProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.RETRY.getName());

        Map<String, String> map = new HashMap<>(2);
        map.put("JDBC Connection Pool", data.dbConnectionId);
        map.put("putsql-sql-statement", data.sqlStatement);


        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.PutSQL.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildSplitJsonProcess(BuildSplitJsonProcessorDTO data) {
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.ORIGINAL.getName());
        Map<String, String> map = new HashMap<>(1);
        map.put("JsonPath Expression", "$.*");
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.SplitJson.getName());
        dto.setPosition(data.getPositionDTO());
        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildCallDbProcedureProcess(BuildCallDbProcedureProcessorDTO buildCallDbProcedureProcessorDTO) {
        List<String> autoRes = new ArrayList<>();
        if (buildCallDbProcedureProcessorDTO.haveNextOne == false) {
            autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        }
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        Map<String, String> map = new HashMap<>(2);
        map.put("Database Connection Pooling Service", buildCallDbProcedureProcessorDTO.dbConnectionId);
        map.put("SQL select query", buildCallDbProcedureProcessorDTO.executsql);
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(buildCallDbProcedureProcessorDTO.details);
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(buildCallDbProcedureProcessorDTO.name);
        dto.setType(ProcessorTypeEnum.ExecuteSQL.getName());
        dto.setPosition(buildCallDbProcedureProcessorDTO.getPositionDTO());
        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        return buildProcessor(buildCallDbProcedureProcessorDTO.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildSqlParameterProcess(DataAccessConfigDTO dataAccessConfigDTO, BuildProcessEvaluateJsonPathDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.UNNMATCHED.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        Map<String, String> map = new HashMap<>();
        //添加字段
        map.put("Destination", "flowfile-attribute");
        List<TableFieldsDTO> tableFieldsList = dataAccessConfigDTO.targetDsConfig.tableFieldsList;
        for (TableFieldsDTO tableFieldsDTO : tableFieldsList) {
            map.put(tableFieldsDTO.fieldName.toLowerCase(), "$." + tableFieldsDTO.fieldName.toLowerCase());
        }
        map.put("fk_doris_increment_code", "$.fk_doris_increment_code");
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.EvaluateJsonPath.getName());
        dto.setPosition(data.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildAssembleSqlProcess(DataAccessConfigDTO dataAccessConfigDTO, BuildReplaceTextProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        Map<String, String> map = new HashMap<>();
        String sql = "insert into " + dataAccessConfigDTO.targetDsConfig.targetTableName.toLowerCase();
        String sqlfiled = " (";
        String sqlValue = " values (";
        //后面把fieldName替换成字段
        String sqlTemplate = "${fieldName:isEmpty():ifElse('null',${fieldName:replace(\"'\",\"''\"):append(\"'\"):prepend(\"'\")})}";
        List<TableFieldsDTO> tableFieldsList = dataAccessConfigDTO.targetDsConfig.tableFieldsList;
        System.out.println("第二次拿到list长度" + tableFieldsList.size());
        for (TableFieldsDTO tableFieldsDTO : tableFieldsList) {
            sqlfiled += tableFieldsDTO.fieldName.toLowerCase() + ",";
            sqlValue += "${" + tableFieldsDTO.fieldName.toLowerCase() + ":isEmpty():ifElse('null',${" + tableFieldsDTO.fieldName.toLowerCase() + ":replace(\"'\",\"''\"):append(\"'\"):prepend(\"'\")})},";
        }
        sqlfiled += "fk_doris_increment_code) ";
        sqlValue += "${fk_doris_increment_code:isEmpty():ifElse('null',${fk_doris_increment_code:replace(\"'\",\"''\"):append(\"'\"):prepend(\"'\")})});";
        sql += sqlfiled + sqlValue;
        map.put("Replacement Value", sql);
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.ReplaceText.getName());
        dto.setPosition(data.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildExecuteSQLRecordProcess(ExecuteSQLRecordDTO executeSQLRecordDTO) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        Map<String, String> map = new HashMap<>();
        map.put("Database Connection Pooling Service", executeSQLRecordDTO.databaseConnectionPoolingService);
        map.put("esqlrecord-record-writer", executeSQLRecordDTO.recordwriter);
        map.put("esql-max-rows", executeSQLRecordDTO.maxRowsPerFlowFile);
        map.put("esql-output-batch-size", executeSQLRecordDTO.outputBatchSize);
        map.put("esql-fetch-size", executeSQLRecordDTO.FetchSize);
        map.put("SQL select query", executeSQLRecordDTO.sqlSelectQuery);
        map.put("dbf-user-logical-types", "true");
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(executeSQLRecordDTO.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(executeSQLRecordDTO.name);
        dto.setType(ProcessorTypeEnum.ExecuteSQLRecord.getName());
        dto.setPosition(executeSQLRecordDTO.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        return buildProcessor(executeSQLRecordDTO.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildPutDatabaseRecordProcess(PutDatabaseRecordDTO putDatabaseRecordDTO) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.RETRY.getName());
        if (Objects.equals(putDatabaseRecordDTO.synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        }
        Map<String, String> map = new HashMap<>();
        map.put("put-db-record-record-reader", putDatabaseRecordDTO.recordReader);
        map.put("db-type", putDatabaseRecordDTO.databaseType);
        map.put("put-db-record-statement-type", putDatabaseRecordDTO.statementType);
        map.put("put-db-record-dcbp-service", putDatabaseRecordDTO.databaseConnectionPoolingService);
        map.put("put-db-record-table-name", putDatabaseRecordDTO.TableName);
        map.put("put-db-record-quoted-identifiers", "true");
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setConcurrentlySchedulableTaskCount(Integer.valueOf(putDatabaseRecordDTO.concurrentTasks));
        config.setComments(putDatabaseRecordDTO.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(putDatabaseRecordDTO.name);
        dto.setType(ProcessorTypeEnum.PutDatabaseRecord.getName());
        dto.setPosition(putDatabaseRecordDTO.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        return buildProcessor(putDatabaseRecordDTO.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildUpdateAttribute(BuildUpdateAttributeDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();

        Map<String, String> map = new HashMap<>(1);
        map.put("Delete Attributes Expression", "sql\\.args\\..*");

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.UpdateAttribute.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildEvaluateJsonPathProcess(BuildProcessEvaluateJsonPathDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.UNNMATCHED.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());

        Map<String, String> map = new HashMap<>(2);
        map.put("Destination", "flowfile-attribute");
        //自定义常量
        for (String selfDefinedParameter : data.selfDefinedParameter) {
            map.put(selfDefinedParameter, "$." + selfDefinedParameter);
        }

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.EvaluateJsonPath.getName());
        dto.setPosition(data.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ConnectionEntity> buildConnectProcessors(String groupId, String sourceId, String targetId, AutoEndBranchTypeEnum type) {
        //目标组件
        ConnectableDTO targetProcessor = NifiHelper.buildConnectableDTO(groupId, targetId);
        //源组件
        ConnectableDTO sourceProcessor = NifiHelper.buildConnectableDTO(groupId, sourceId);
        //连接条件
        List<String> relationships = new ArrayList<>();
        relationships.add(type.getName());

        //连接器属性
        ConnectionDTO dto = new ConnectionDTO();
        dto.setDestination(targetProcessor);
        dto.setSource(sourceProcessor);
        dto.setName("test connect");
        dto.setSelectedRelationships(relationships);

        ConnectionEntity entity = new ConnectionEntity();
        entity.setComponent(dto);
        entity.setRevision(NifiHelper.buildRevisionDTO());

        try {
            ConnectionEntity res = NifiHelper.getProcessGroupsApi().createConnection(groupId, entity);
            return BusinessResult.of(true, "连接器创建成功", res);
        } catch (ApiException e) {
            log.error("连接器创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "连接器创建失败" + e.getMessage(), null);
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ConnectionEntity> buildConnectProcessor(String groupId, String sourceId, String targetId, List<AutoEndBranchTypeEnum> type) {
        //目标组件
        ConnectableDTO targetProcessor = NifiHelper.buildConnectableDTO(groupId, targetId);
        //源组件
        ConnectableDTO sourceProcessor = NifiHelper.buildConnectableDTO(groupId, sourceId);
        //连接条件
        List<String> relationships = new ArrayList<>();
        for (AutoEndBranchTypeEnum autoEndBranchTypeEnum : type) {
            relationships.add(autoEndBranchTypeEnum.getName());
        }

        //连接器属性
        ConnectionDTO dto = new ConnectionDTO();
        dto.setDestination(targetProcessor);
        dto.setSource(sourceProcessor);
        dto.setName("test connect");
        dto.setSelectedRelationships(relationships);

        ConnectionEntity entity = new ConnectionEntity();
        entity.setComponent(dto);
        entity.setRevision(NifiHelper.buildRevisionDTO());

        try {
            ConnectionEntity res = NifiHelper.getProcessGroupsApi().createConnection(groupId, entity);
            return BusinessResult.of(true, "连接器创建成功", res);
        } catch (ApiException e) {
            log.error("连接器创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "连接器创建失败" + e.getMessage(), null);
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessGroupsVO> getAllGroups(String groupId) {
        try {
            groupId = StringUtils.isEmpty(groupId) ? NifiConstants.ApiConstants.ROOT_NODE : groupId;
            String url = basePath + NifiConstants.ApiConstants.ALL_GROUP_RUN_STATUS.replace("{id}", groupId);
            ResponseEntity<ProcessGroupsVO> res = httpClient.exchange(url, HttpMethod.GET, null, ProcessGroupsVO.class);
            if (res.getStatusCode() == HttpStatus.OK) {
                return BusinessResult.of(true, "查询成功", res.getBody());
            } else {
                return BusinessResult.of(false, "查询分组报错", null);
            }
        } catch (Exception e) {
            log.error("查询分组报错，【" + e.getMessage() + "】", e);
            return BusinessResult.of(false, "查询分组报错，【" + e.getMessage() + "】", null);
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public int getGroupCount(String groupId) {
        try {
            BusinessResult<ProcessGroupsVO> res = getAllGroups(groupId);
            return res.data == null ? 0 : getAllGroups(groupId).data.processGroups.size();
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public List<ProcessorEntity> enabledProcessor(String groupId, ProcessorEntity... entities) {
        return enabledProcessor(groupId, Arrays.stream(entities).collect(Collectors.toList()));
    }

    @Override
    public List<ProcessorEntity> enabledProcessor(String groupId, List<ProcessorEntity> entities) {
        List<ProcessorEntity> res = new ArrayList<>();
        ProcessorsApi apiClient = NifiHelper.getProcessorsApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        for (ProcessorEntity item : entities) {
            try {
                ProcessorEntity entity = apiClient.getProcessor(item.getId());
                if (entity.getComponent().getState() == ProcessorDTO.StateEnum.RUNNING) {
                    continue;
                }

                ProcessorRunStatusEntity dto = new ProcessorRunStatusEntity();
                dto.state = ProcessorDTO.StateEnum.RUNNING.toString();
                dto.disconnectedNodeAcknowledged = true;
                dto.revision = entity.getRevision();

                HttpEntity<ProcessorRunStatusEntity> request = new HttpEntity<>(dto, headers);

                String url = basePath + NifiConstants.ApiConstants.PROCESSOR_RUN_STATUS.replace("{id}", item.getId());
                ResponseEntity<String> response = httpClient.exchange(url, HttpMethod.PUT, request, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    ProcessorEntity newEntity = getProcessor(item.getId());
                    if (newEntity != null && newEntity.getComponent().getState() == ProcessorDTO.StateEnum.RUNNING) {
                        res.add(newEntity);
                    }
                }
            } catch (ApiException e) {
                log.error("【" + item.getId() + "】【" + item.getComponent().getType() + "】运行组件失败，【" + e.getResponseBody() + "】: ", e);
            }
        }
        return res;
    }

    @Override
    public List<ProcessorEntity> stopProcessor(String groupId, List<ProcessorEntity> entities) {
        List<ProcessorEntity> res = new ArrayList<>();
        ProcessorsApi apiClient = NifiHelper.getProcessorsApi();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        for (ProcessorEntity item : entities) {
            try {
                ProcessorEntity entity = apiClient.getProcessor(item.getId());
                if (entity.getComponent().getState() == ProcessorDTO.StateEnum.STOPPED) {
                    continue;
                }

                ProcessorRunStatusEntity dto = new ProcessorRunStatusEntity();
                dto.state = ProcessorDTO.StateEnum.STOPPED.toString();
                dto.disconnectedNodeAcknowledged = true;
                dto.revision = entity.getRevision();

                HttpEntity<ProcessorRunStatusEntity> request = new HttpEntity<>(dto, headers);

                String url = basePath + NifiConstants.ApiConstants.PROCESSOR_RUN_STATUS.replace("{id}", item.getId());
                ResponseEntity<String> response = httpClient.exchange(url, HttpMethod.PUT, request, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    ProcessorEntity newEntity = getProcessor(item.getId());
                    if (newEntity != null && newEntity.getComponent().getState() == ProcessorDTO.StateEnum.RUNNING) {
                        res.add(newEntity);
                    }
                }
            } catch (ApiException e) {
                log.error("【" + item.getId() + "】【" + item.getComponent().getType() + "】运行组件失败，【" + e.getResponseBody() + "】: ", e);
            }
        }
        return res;
    }

    @Override
    public List<ProcessorEntity> updateProcessorConfig(String groupId, List<ProcessorEntity> entities) {
        List<ProcessorEntity> res = new ArrayList<>();
        ProcessorsApi apiClient = NifiHelper.getProcessorsApi();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        for (ProcessorEntity item : entities) {
            try {
                ProcessorEntity entity = apiClient.getProcessor(item.getId());
                entity.getComponent().getConfig().setSchedulingPeriod(item.getComponent().getConfig().getSchedulingPeriod());
                entity.getComponent().getConfig().setSchedulingStrategy(item.getComponent().getConfig().getSchedulingStrategy());
                if (entity.getComponent().getState() == ProcessorDTO.StateEnum.RUNNING) {
                    continue;
                }
                HttpEntity<ProcessorEntity> request = new HttpEntity<>(entity, headers);

                String url = basePath + NifiConstants.ApiConstants.PUTPROCESS.replace("{id}", item.getId());
                ResponseEntity<String> response = httpClient.exchange(url, HttpMethod.PUT, request, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    ProcessorEntity newEntity = getProcessor(item.getId());
                    if (newEntity != null && newEntity.getComponent().getState() == ProcessorDTO.StateEnum.RUNNING) {
                        res.add(newEntity);
                    }
                }
            } catch (ApiException e) {
                log.error("【" + item.getId() + "】【" + item.getComponent().getType() + "】运行组件失败，【" + e.getResponseBody() + "】: ", e);
            }
        }
        return res;
    }

    @Override
    public ProcessorEntity getProcessor(String id) {
        try {
            return NifiHelper.getProcessorsApi().getProcessor(id);
        } catch (ApiException e) {
            log.error("【" + id + "】查询组件报错，", e);
            return null;
        }
    }

    /**
     * 创建Processor组件
     *
     * @param groupId 组id
     * @param entity  组件基础信息
     * @param dto     具体组件信息
     * @param config  组件配置
     * @return 调用结果
     */
    private BusinessResult<ProcessorEntity> buildProcessor(String groupId, ProcessorEntity entity, ProcessorDTO dto, ProcessorConfigDTO config) {
        try {
            dto.setConfig(config);
            entity.setComponent(dto);
            ProcessorEntity res = NifiHelper.getProcessGroupsApi().createProcessor(groupId, entity);
            return BusinessResult.of(true, "【" + dto.getType() + "】组件创建完成", res);
        } catch (ApiException e) {
            log.error("【" + dto.getType() + "】组件创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "【" + dto.getType() + "】组件创建失败" + e.getMessage(), null);
        }
    }

    /*
     * 修改任务组调度
     * groupId             任务组id
     * ProcessorId         组件id
     * schedulingStrategy  调度方式
     * schedulingPeriod    调度频率
     * */
    @Override
    public ResultEnum modifyScheduling(String groupId, String ProcessorId, String schedulingStrategy, String schedulingPeriod) {
        try {
            ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor(ProcessorId);
            List<ProcessorEntity> processorEntities = new ArrayList<>();
            processorEntities.add(processor);
            //先停止组件
            this.stopProcessor(groupId, processorEntities);
            //修改调度
            processor.getComponent().getConfig().setSchedulingStrategy(schedulingStrategy);
            processor.getComponent().getConfig().setSchedulingPeriod(schedulingPeriod);
            this.updateProcessorConfig(groupId, processorEntities);
            //启动组件
            this.enabledProcessor(groupId, processorEntities);
            return ResultEnum.SUCCESS;
        } catch (ApiException e) {
            log.error("调度修改失败，【" + e.getResponseBody() + "】: ", e);
            return ResultEnum.TASK_NIFI_DISPATCH_ERROR;
        }
    }

    /*
     * emptyNifiConnectionQueue  清空nifi连接队列
     * groupId                   任务组id
     * */
    @Override
    public ResultEnum emptyNifiConnectionQueue(String groupId) {
        try {
            String url = basePath + NifiConstants.ApiConstants.EMPTY_ALL_CONNECTIONS_REQUESTS.replace("{id}", groupId);
            ResponseEntity<String> response = httpClient.exchange(url, HttpMethod.POST, null, String.class);
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("调度修改失败: ", e);
            return ResultEnum.TASK_NIFI_EMPTY_ALL_CONNECTIONS_REQUESTS_ERROR;
        }
    }

    /*
     * 删除Input
     * */
    @Override
    public ResultEnum deleteNifiInputProcessor(List<PortEntity> portEntities) {
        for (PortEntity portEntity : portEntities) {
            try {
                NifiHelper.getInputPortsApi().removeInputPort(portEntity.getId(), String.valueOf(portEntity.getRevision().getVersion() + 1), null, null);
            } catch (ApiException e) {
                log.error("id=" + portEntity.getId() + "，，【" + e.getResponseBody() + "】: ", e);
            }
        }
        return ResultEnum.SUCCESS;
    }

    /*
     * 删除Output
     * */
    @Override
    public ResultEnum deleteNifiOutputProcessor(List<PortEntity> portEntities) {
        for (PortEntity portEntity : portEntities) {
            try {
                NifiHelper.getOutputPortsApi().removeOutputPort(portEntity.getId(), String.valueOf(portEntity.getRevision().getVersion() + 1), null, null);
            } catch (ApiException e) {
                log.error("id=" + portEntity.getId() + "，，【" + e.getResponseBody() + "】: ", e);
            }
        }
        return ResultEnum.SUCCESS;
    }

    /*
     * 暂停OutputStatus
     * */
    @Override
    public ResultEnum updateOutputStatus(List<PortEntity> portEntities, PortRunStatusEntity portRunStatusEntity) {
        for (PortEntity portEntity : portEntities) {
            portRunStatusEntity.setState(PortRunStatusEntity.StateEnum.STOPPED);
            portRunStatusEntity.setRevision(portEntity.getRevision());
            portRunStatusEntity.setDisconnectedNodeAcknowledged(true);
            try {
                NifiHelper.getOutputPortsApi().updateRunStatus(portEntity.getId(), portRunStatusEntity);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

        return ResultEnum.SUCCESS;
    }

    /*
     *暂停InputStatus
     * */
    @Override
    public ResultEnum updateInputStatus(List<PortEntity> portEntities, PortRunStatusEntity portRunStatusEntity) {
        for (PortEntity portEntity : portEntities) {
            portRunStatusEntity.setRevision(portEntity.getRevision());
            portRunStatusEntity.setDisconnectedNodeAcknowledged(true);
            try {
                NifiHelper.getInputPortsApi().updateRunStatus(portEntity.getId(), portRunStatusEntity);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
        return ResultEnum.SUCCESS;
    }


    /*
     * controllerServicesRunStatus   禁用控制器服务
     * */
    @Override
    public ResultEnum controllerServicesRunStatus(String controllerServicesId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            ControllerServiceRunStatusEntity controllerServiceRunStatusEntity = new ControllerServiceRunStatusEntity();
            ControllerServiceEntity controllerService = NifiHelper.getControllerServicesApi().getControllerService(controllerServicesId);
            controllerServiceRunStatusEntity.setRevision(controllerService.getRevision());
            controllerServiceRunStatusEntity.setDisconnectedNodeAcknowledged(false);
            controllerServiceRunStatusEntity.setState(ControllerServiceRunStatusEntity.StateEnum.DISABLED);
            HttpEntity<ControllerServiceRunStatusEntity> request = new HttpEntity<>(controllerServiceRunStatusEntity, headers);
            String url1 = basePath + NifiConstants.ApiConstants.CONTROLLER_SERVICES_RUN_STATUS.replace("{id}", controllerServicesId);
            ResponseEntity<String> response1 = httpClient.exchange(url1, HttpMethod.PUT, request, String.class);
            return ResultEnum.SUCCESS;
        } catch (ApiException e) {
            log.error("禁用控制器服务失败，【" + e.getResponseBody() + "】: ", e);
            return ResultEnum.TASK_NIFI_DISPATCH_ERROR;
        }
    }

    /*
     * deleteNifiFlow       删除nifi流程
     * nifiRemoveDTOList
     * */
    @Override
    public ResultEnum deleteNifiFlow(DataModelVO dataModelVO) {
        try {
            List<NifiRemoveDTO> nifiRemoveDTOList = createNifiRemoveDTOs(dataModelVO);

            for (NifiRemoveDTO nifiRemoveDTO : nifiRemoveDTOList) {
                List<ProcessorEntity> processorEntities = new ArrayList<>();
                List<PortEntity> inputPortEntities = new ArrayList<>();
                List<PortEntity> outputPortEntities = new ArrayList<>();
                for (String ProcessId : nifiRemoveDTO.ProcessIds) {
                    if (ProcessId != null && ProcessId != "") {
                        ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor(ProcessId);
                        processorEntities.add(processor);
                    }
                }
                //暂停13个组件
                this.stopProcessor(nifiRemoveDTO.groupId, processorEntities);
                ScheduleComponentsEntity scheduleComponentsEntity = new ScheduleComponentsEntity();
                scheduleComponentsEntity.setId(nifiRemoveDTO.groupId);
                scheduleComponentsEntity.setDisconnectedNodeAcknowledged(false);
                scheduleComponentsEntity.setState(ScheduleComponentsEntity.StateEnum.STOPPED);
                NifiHelper.getFlowApi().scheduleComponents(nifiRemoveDTO.groupId, scheduleComponentsEntity);
                for (ProcessorEntity processorEntity : processorEntities) {
                    //terminateProcessorCall
                    NifiHelper.getProcessorsApi().terminateProcessor(processorEntity.getId());
                }
                //清空队列
                this.emptyNifiConnectionQueue(nifiRemoveDTO.groupId);
                //禁用2个控制器服务 ,分开写是因为有时候禁用不及时,导致删除的时候还没禁用,删除失败
                for (String controllerServicesId : nifiRemoveDTO.controllerServicesIds.subList(0, 4)) {
                    if (controllerServicesId != null) {
                        //禁用
                        this.controllerServicesRunStatus(controllerServicesId);
                    }
                }
                for (String controllerServicesId : nifiRemoveDTO.controllerServicesIds.subList(0, 4)) {
                    if (controllerServicesId != null) {
                        //删除
                        ControllerServiceEntity controllerService = NifiHelper.getControllerServicesApi().getControllerService(controllerServicesId);
                        NifiHelper.getControllerServicesApi().removeControllerService(controllerServicesId, controllerService.getRevision().getVersion().toString(), "", false);
                    }
                }
                //暂停,删除input和output,删除任务组
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(nifiRemoveDTO.groupId);
                //操作input与output组件
                operatePorts(nifiRemoveDTO, inputPortEntities, outputPortEntities);
                //删除13个组件
                for (ProcessorEntity processorEntity : processorEntities) {
                    //因为版本变了,所以要再查一遍
                    ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor(processorEntity.getId());
                    NifiHelper.getProcessorsApi().deleteProcessor(processor.getId(), String.valueOf(processor.getRevision().getVersion()), null, null);
                }
                NifiHelper.getProcessGroupsApi().removeProcessGroup(processGroup.getId(), String.valueOf(processGroup.getRevision().getVersion()), null, null);
                if (!Objects.equals(OlapTableEnum.PHYSICS, nifiRemoveDTO.olapTableEnum)) {
                    tableNifiSettingService.removeById(nifiRemoveDTO.tableId);
                }
            }
            //删除应用
            if (nifiRemoveDTOList.size() != 0 && nifiRemoveDTOList.get(0).delApp) {
                //禁用2个控制器服务
                for (String controllerServicesId : nifiRemoveDTOList.get(0).controllerServicesIds.subList(5, 6)) {
                    if (controllerServicesId != null) {
                        this.controllerServicesRunStatus(controllerServicesId);
                    }
                }
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(nifiRemoveDTOList.get(0).appId);
                NifiHelper.getProcessGroupsApi().removeProcessGroup(processGroup.getId(), String.valueOf(processGroup.getRevision().getVersion()), null, null);
                appNifiSettingService.removeById(dataModelVO.businessId);
            }
            return ResultEnum.SUCCESS;
        } catch (ApiException e) {
            log.error("nifi删除失败，【" + e.getResponseBody() + "】: ", e);
            return ResultEnum.TASK_NIFI_DELETE_FLOW;
        }
    }

    /*
     * 删除inputoutput组件
     * */
    private void operatePorts(NifiRemoveDTO nifiRemoveDTO, List<PortEntity> inputPortEntities, List<PortEntity> outputPortEntities) {
        try {
            //1.删除连接线
            for (String inputportConnectId : nifiRemoveDTO.inputportConnectIds) {
                if (inputportConnectId != null && inputportConnectId != "") {
                    ConnectionEntity connection = NifiHelper.getConnectionsApi().getConnection(inputportConnectId);
                    NifiHelper.getConnectionsApi().deleteConnection(connection.getId(), String.valueOf(connection.getRevision().getVersion()), null, null);
                }
            }
            for (String outputportConnectId : nifiRemoveDTO.outputportConnectIds) {
                if (outputportConnectId != null && outputportConnectId != "") {
                    ConnectionEntity connection = NifiHelper.getConnectionsApi().getConnection(outputportConnectId);
                    NifiHelper.getConnectionsApi().deleteConnection(connection.getId(), String.valueOf(connection.getRevision().getVersion()), null, null);
                }
            }

            PortRunStatusEntity portRunStatusEntity = new PortRunStatusEntity();
            portRunStatusEntity.setState(PortRunStatusEntity.StateEnum.STOPPED);
            for (String id : nifiRemoveDTO.inputPortIds) {
                if (id != null && id != "") {
                    PortEntity inputPort = NifiHelper.getInputPortsApi().getInputPort(id);
                    inputPortEntities.add(inputPort);
                }
            }
            for (String id : nifiRemoveDTO.outputPortIds) {
                if (id != null && id != "") {
                    PortEntity outputPort = NifiHelper.getOutputPortsApi().getOutputPort(id);
                    outputPortEntities.add(outputPort);
                }
            }
            if (inputPortEntities != null && inputPortEntities.size() != 0 && outputPortEntities != null && outputPortEntities.size() != 0) {
                this.updateInputStatus(inputPortEntities, portRunStatusEntity);
                this.updateOutputStatus(outputPortEntities, portRunStatusEntity);
                this.deleteNifiInputProcessor(inputPortEntities);
                this.deleteNifiOutputProcessor(outputPortEntities);
            }
        } catch (ApiException e) {
            log.error("nifi--port模块删除失败，【" + e.getResponseBody() + "】: ", e);
        }
    }

    private List<NifiRemoveDTO> createNifiRemoveDTOs(DataModelVO dataModelVO) {
        List<NifiRemoveDTO> nifiRemoveDTOS = new ArrayList<>();
        AppNifiSettingPO appNifiSettingPO = appNifiSettingService.query().eq("app_id", dataModelVO.businessId).eq("type", dataModelVO.dataClassifyEnum.getValue()).eq("del_flag", 1).one();
        //维度表
        List<NifiRemoveDTO> nifiRemoveList1 = createNifiRemoveList(dataModelVO.businessId, dataModelVO.dimensionIdList, appNifiSettingPO, dataModelVO.delBusiness);
        nifiRemoveDTOS.addAll(nifiRemoveList1);
        //事实表
        List<NifiRemoveDTO> nifiRemoveList2 = createNifiRemoveList(dataModelVO.businessId, dataModelVO.factIdList, appNifiSettingPO, dataModelVO.delBusiness);
        nifiRemoveDTOS.addAll(nifiRemoveList2);
        //物理表
        List<NifiRemoveDTO> nifiRemoveList3 = createNifiRemoveList(dataModelVO.businessId, dataModelVO.physicsIdList, appNifiSettingPO, dataModelVO.delBusiness);
        nifiRemoveDTOS.addAll(nifiRemoveList3);
        //指标表
        List<NifiRemoveDTO> nifiRemoveList4 = createNifiRemoveList(dataModelVO.businessId, dataModelVO.indicatorIdList, appNifiSettingPO, dataModelVO.delBusiness);
        nifiRemoveDTOS.addAll(nifiRemoveList4);
        return nifiRemoveDTOS;
    }

    private List<NifiRemoveDTO> createNifiRemoveList(String businessId, DataModelTableVO dataModelTableVO, AppNifiSettingPO appNifiSettingPO, Boolean delApp) {
        List<NifiRemoveDTO> nifiRemoveDTOS = new ArrayList<>();
        NifiRemoveDTO nifiRemoveDTO = new NifiRemoveDTO();
        if (dataModelTableVO != null && dataModelTableVO.ids != null) {
            for (Long tableId : dataModelTableVO.ids) {
                List<String> ProcessIds = new ArrayList<>();
                List<String> controllerServicesIds = new ArrayList<>();
                List<String> inputPortIds = new ArrayList<>();
                List<String> outputPortIds = new ArrayList<>();
                List<String> inputportConnectIds = new ArrayList<>();
                List<String> outputportConnectIds = new ArrayList<>();
                TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", businessId).eq("table_access_id", tableId).eq("type", dataModelTableVO.type.getValue()).eq("del_flag", 1).one();
                //删除topic_name
                TableTopicDTO topicDTO = new TableTopicDTO();
                topicDTO.tableId = tableNifiSettingPO.tableAccessId;
                topicDTO.tableType = tableNifiSettingPO.type;
                tableTopic.deleteTableTopicByTableId(topicDTO);
                if (tableNifiSettingPO == null) {
                    continue;
                }
                nifiRemoveDTO.delApp = delApp;
                controllerServicesIds.add(tableNifiSettingPO.avroRecordSetWriterId);
                controllerServicesIds.add(tableNifiSettingPO.putDatabaseRecordId);
                controllerServicesIds.add(tableNifiSettingPO.convertAvroRecordSetWriterId);
                controllerServicesIds.add(tableNifiSettingPO.convertPutDatabaseRecordId);
                controllerServicesIds.add(tableNifiSettingPO.convertAvroRecordSetWriterForCodeId);
                controllerServicesIds.add(tableNifiSettingPO.convertPutDatabaseRecordForCodeId);
                controllerServicesIds.add(tableNifiSettingPO.csvReaderId);
                controllerServicesIds.add(appNifiSettingPO.targetDbPoolComponentId);
                controllerServicesIds.add(appNifiSettingPO.sourceDbPoolComponentId);
                //连接通道id
                inputPortIds.add(tableNifiSettingPO.tableInputPortId);
                inputPortIds.add(tableNifiSettingPO.processorInputPortId);
                outputPortIds.add(tableNifiSettingPO.processorOutputPortId);
                outputPortIds.add(tableNifiSettingPO.tableOutputPortId);
                //通道连接线
                inputportConnectIds.add(tableNifiSettingPO.tableInputPortConnectId);
                inputportConnectIds.add(tableNifiSettingPO.processorInputPortConnectId);
                outputportConnectIds.add(tableNifiSettingPO.processorOutputPortConnectId);
                outputportConnectIds.add(tableNifiSettingPO.tableOutputPortConnectId);
                //组件
                ProcessIds.add(tableNifiSettingPO.dispatchComponentId);
                ProcessIds.add(tableNifiSettingPO.publishKafkaProcessorId);
                ProcessIds.add(tableNifiSettingPO.consumeKafkaProcessorId);
                ProcessIds.add(tableNifiSettingPO.queryIncrementProcessorId);
                ProcessIds.add(tableNifiSettingPO.convertDataToJsonProcessorId);
                ProcessIds.add(tableNifiSettingPO.setIncrementProcessorId);
                ProcessIds.add(tableNifiSettingPO.putLogToConfigDbProcessorId);
                ProcessIds.add(tableNifiSettingPO.executeTargetDeleteProcessorId);
                ProcessIds.add(tableNifiSettingPO.executeSqlRecordProcessorId);
                ProcessIds.add(tableNifiSettingPO.getFtpProcessorId);
                ProcessIds.add(tableNifiSettingPO.convertExcelToCsvProcessorId);
                ProcessIds.add(tableNifiSettingPO.convertRecordProcessorId);
                ProcessIds.add(tableNifiSettingPO.updateFieldProcessorId);
                ProcessIds.add(tableNifiSettingPO.updateFieldForCodeProcessorId);
                ProcessIds.add(tableNifiSettingPO.saveTargetDbProcessorId);
                ProcessIds.add(tableNifiSettingPO.generateFlowFileProcessorId);
                ProcessIds.add(tableNifiSettingPO.invokeHttpProcessorId);
                //ProcessIds.add(tableNifiSettingPO.mergeContentProcessorId);
                ProcessIds.add(tableNifiSettingPO.odsToStgProcessorId);
                ProcessIds.add(tableNifiSettingPO.queryNumbersProcessorId);
                ProcessIds.add(tableNifiSettingPO.convertNumbersToJsonProcessorId);
                ProcessIds.add(tableNifiSettingPO.setNumbersProcessorId);
                ProcessIds.add(tableNifiSettingPO.saveNumbersProcessorId);
                ProcessIds.add(tableNifiSettingPO.publishKafkaPipelineProcessorId);
                ProcessIds.add(tableNifiSettingPO.queryForSupervisionProcessorId);
                ProcessIds.add(tableNifiSettingPO.convertJsonForSupervisionProcessorId);
                ProcessIds.add(tableNifiSettingPO.publishKafkaForSupervisionProcessorId);
                nifiRemoveDTO.appId = appNifiSettingPO.appComponentId;
                nifiRemoveDTO.ProcessIds = ProcessIds;
                nifiRemoveDTO.controllerServicesIds = controllerServicesIds;
                nifiRemoveDTO.inputPortIds = inputPortIds;
                nifiRemoveDTO.outputPortIds = outputPortIds;
                nifiRemoveDTO.groupId = tableNifiSettingPO.tableComponentId;
                nifiRemoveDTO.tableId = tableId;
                nifiRemoveDTO.olapTableEnum = dataModelTableVO.type;
                nifiRemoveDTO.inputportConnectIds = inputportConnectIds;
                nifiRemoveDTO.outputportConnectIds = outputportConnectIds;
                nifiRemoveDTOS.add(nifiRemoveDTO);
            }
        }
        return nifiRemoveDTOS;
    }


    /**
     * 创建input port组件
     *
     * @param buildPortDTO buildPortDTO
     * @return 返回值
     */
    @Override
    public PortEntity buildInputPort(BuildPortDTO buildPortDTO) {

        PortEntity body = new PortEntity();

        RevisionDTO revisionDTO = new RevisionDTO();
        revisionDTO.setClientId(buildPortDTO.clientId);
        revisionDTO.setVersion(0L);

        PortDTO component = new PortDTO();
        component.setName(buildPortDTO.portName + NifiConstants.PortConstants.INPUT_PORT_NAME);
        // 是否允许远程访问
        component.setAllowRemoteAccess(false);

        // 坐标
        PositionDTO positionDTO = new PositionDTO();
        positionDTO.setX(buildPortDTO.componentX);
        positionDTO.setY(buildPortDTO.componentY);

        body.setDisconnectedNodeAcknowledged(false);
        body.setRevision(revisionDTO);
        component.setPosition(positionDTO);
        body.setComponent(component);

        String uri = basePath + NifiConstants.ApiConstants.CREATE_INPUT_PORT.replace("{id}", buildPortDTO.componentId);
        return sendHttpRequest(body, uri);
    }

    /**
     * 创建output port组件
     *
     * @param buildPortDTO buildPortDTO
     * @return 返回值
     */
    @Override
    public PortEntity buildOutputPort(BuildPortDTO buildPortDTO) {

        PortEntity body = new PortEntity();

        RevisionDTO revisionDTO = new RevisionDTO();
        revisionDTO.setClientId(buildPortDTO.clientId);
        revisionDTO.setVersion(0L);

        PortDTO component = new PortDTO();
        component.setName(buildPortDTO.portName + NifiConstants.PortConstants.OUTPUT_PORT_NAME);
        // 是否允许远程访问
        component.setAllowRemoteAccess(false);

        // 坐标
        PositionDTO positionDTO = new PositionDTO();
        positionDTO.setX(buildPortDTO.componentX);
        positionDTO.setY(buildPortDTO.componentY);

        body.setDisconnectedNodeAcknowledged(false);
        body.setRevision(revisionDTO);
        component.setPosition(positionDTO);
        body.setComponent(component);

        String uri = basePath + NifiConstants.ApiConstants.CREATE_OUTPUT_PORT.replace("{id}", buildPortDTO.componentId);
        return sendHttpRequest(body, uri);
    }

    /**
     * 创建input_port连接
     *
     * @param buildConnectDTO buildConnectDTO
     * @return 执行结果
     */
    @Override
    public ConnectionEntity buildInputPortConnections(BuildConnectDTO buildConnectDTO) {

        ConnectionEntity body = new ConnectionEntity();
        RevisionDTO revisionDTO = new RevisionDTO();
        revisionDTO.version(0L);

        ConnectionDTO component = new ConnectionDTO();
        ConnectableDTO destination = new ConnectableDTO();
        // 当前组件在哪个组下的组件id
        destination.setGroupId(buildConnectDTO.destination.groupId);
        // input_port连接的组件 id(目标组件id)
        destination.setId(buildConnectDTO.destination.id);
        // 目标组件类型
        destination.type(buildConnectDTO.destination.typeEnum);

        ConnectableDTO source = new ConnectableDTO();
        // 当前组件在哪个组下的组件id
        source.setGroupId(buildConnectDTO.source.groupId);
        // input_port的组件id(源组件id)
        source.setId(buildConnectDTO.source.id);
        // 源组件类型
        source.setType(buildConnectDTO.source.typeEnum);

        component.setDestination(destination);
        component.setSource(source);
        if (Objects.equals(buildConnectDTO.loadBalanceStrategyEnum, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE)) {
            component.setLoadBalanceCompression(ConnectionDTO.LoadBalanceCompressionEnum.DO_NOT_COMPRESS);
            component.setLoadBalanceStrategy(ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);
        }
        // 构造的参数
        body.setRevision(revisionDTO);
        body.setDisconnectedNodeAcknowledged(false);
        body.setComponent(component);
        body.setDestinationType(ConnectionEntity.DestinationTypeEnum.INPUT_PORT);

        String uri = basePath + NifiConstants.ApiConstants
                .CREATE_CONNECTIONS.replace("{id}", buildConnectDTO.fatherComponentId);

        // 发送请求
        return sendHttpRequest(body, uri);
    }

    /**
     * 创建output_port连接
     *
     * @param buildConnectDTO buildConnectDTO
     * @return 返回值
     */
    @Override
    public ConnectionEntity buildOutPortPortConnections(BuildConnectDTO buildConnectDTO) {

        ConnectionEntity body = new ConnectionEntity();
        RevisionDTO revisionDTO = new RevisionDTO();
        revisionDTO.version(0L);

        ConnectionDTO component = new ConnectionDTO();
        ConnectableDTO destination = new ConnectableDTO();
        // 当前组件在哪个组下的组件id
        destination.setGroupId(buildConnectDTO.destination.groupId);
        // 目标组件id
        destination.setId(buildConnectDTO.destination.id);
        // 目标组件类型
        destination.type(buildConnectDTO.destination.typeEnum);

        ConnectableDTO source = new ConnectableDTO();
        // 当前组件在哪个组下的组件id
        source.setGroupId(buildConnectDTO.source.groupId);
        // 源组件 id
        source.setId(buildConnectDTO.source.id);
        // 源组件类型
        source.setType(buildConnectDTO.source.typeEnum);

        // 第二层output_port连线没有这个参数
        List<String> selectedRelationships = new ArrayList<>();
        if (buildConnectDTO.level == 3) {

            selectedRelationships.add("success");
            component.setSelectedRelationships(selectedRelationships);
        } else if (buildConnectDTO.level == 4) {
            selectedRelationships.add("wait");
            component.setSelectedRelationships(selectedRelationships);
        } else if (buildConnectDTO.level == 5) {
            selectedRelationships.add("matched");
            component.setSelectedRelationships(selectedRelationships);
        }
        if (Objects.equals(buildConnectDTO.loadBalanceStrategyEnum, ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE)) {
            component.setLoadBalanceCompression(ConnectionDTO.LoadBalanceCompressionEnum.DO_NOT_COMPRESS);
            component.setLoadBalanceStrategy(ConnectionDTO.LoadBalanceStrategyEnum.SINGLE_NODE);
        }

        component.setDestination(destination);
        component.setSource(source);
        // 构造的参数
        body.setRevision(revisionDTO);
        body.setDisconnectedNodeAcknowledged(false);
        body.setComponent(component);

        String uri = basePath + NifiConstants.ApiConstants
                .CREATE_CONNECTIONS.replace("{id}", buildConnectDTO.fatherComponentId);

        return sendHttpRequest(body, uri);
    }

    /**
     * 创建port组件连接请求
     *
     * @param body body
     * @param uri  uri
     * @return 返回值
     */
    private ConnectionEntity sendHttpRequest(ConnectionEntity body, String uri) {
        String json = JSON.toJSONString(body);

        HttpClient client = new DefaultHttpClient();
        // post请求
        HttpPost request = new HttpPost(uri);

        request.setHeader("Content-Type", "application/json; charset=utf-8");
        ConnectionEntity connectionEntity = new ConnectionEntity();
        try {
            request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
            HttpResponse resp = client.execute(request);
            org.apache.http.HttpEntity entity = resp.getEntity();
            System.out.println(entity.toString());
            //解析返回数据
            String result = EntityUtils.toString(entity, "UTF-8");

            connectionEntity = JSON.parseObject(result, ConnectionEntity.class);
            log.info("执行sendHttpRequest方法成功,【返回信息为：】,{}", result);
        } catch (Exception e) {
            log.error("执行sendHttpRequest方法失败,【失败原因为：】,{}", e.getMessage());
        }

        return connectionEntity;
    }

    /**
     * 创建port组件请求
     *
     * @param body body
     * @param uri  uri
     * @return 返回值
     */
    private PortEntity sendHttpRequest(PortEntity body, String uri) {
        String json = JSON.toJSONString(body);

        HttpClient client = new DefaultHttpClient();
        // post请求
        HttpPost request = new HttpPost(uri);

        request.setHeader("Content-Type", "application/json; charset=utf-8");
        PortEntity portEntity = new PortEntity();
        try {
            request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
            HttpResponse resp = client.execute(request);
            org.apache.http.HttpEntity entity = resp.getEntity();
            System.out.println(entity.toString());
            //解析返回数据
            String result = EntityUtils.toString(entity, "UTF-8");

            portEntity = JSON.parseObject(result, PortEntity.class);
            log.info("执行sendHttpRequest方法成功,【返回信息为：】,{}", result);
        } catch (Exception e) {
            log.error("执行sendHttpRequest方法失败,【失败原因为：】,{}", e.getMessage());
        }

        return portEntity;
    }


    /*
     *创建RedisConnectionPoolService控制器服务
     * */
    @Override
    public BusinessResult<ControllerServiceEntity> createRedisConnectionPoolService(BuildRedisConnectionPoolServiceDTO data) {

        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(1);
        map.put("Connection String", data.connectionString);

        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setType(ControllerServiceTypeEnum.REDISCONNECTIONPOOL.getName());
        dto.setName(data.name);
        dto.setComments(data.details);
        dto.setProperties(map);

        entity.setPosition(data.positionDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setComponent(dto);

        try {
            ControllerServiceEntity res = NifiHelper.getProcessGroupsApi().createControllerService(data.groupId, entity);
            //是否将控制器服务打开
            if (data.enabled) {
                BusinessResult<ControllerServiceEntity> model = updateDbControllerServiceState(res);
                if (model.success) {
                    return BusinessResult.of(true, "控制器服务创建成功，并开启运行", res);
                } else {
                    return BusinessResult.of(false, model.msg, null);
                }
            }
            return BusinessResult.of(true, "控制器服务创建成功", res);
        } catch (ApiException e) {
            log.error("创建连接池失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "创建连接池失败: " + e.getMessage(), null);
        }
    }


    /*
     * 创建RedisDistributedMapCacheClientService控制器服务
     * */
    @Override
    public BusinessResult<ControllerServiceEntity> createRedisDistributedMapCacheClientService(BuildRedisDistributedMapCacheClientServiceDTO data) {

        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(1);
        map.put("redis-connection-pool", data.redisConnectionPool);

        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setType(ControllerServiceTypeEnum.REDISDISTRIBUTEMAPCACHECLIENTSERVICE.getName());
        dto.setName(data.name);
        dto.setComments(data.details);
        dto.setProperties(map);

        entity.setPosition(data.positionDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setComponent(dto);

        try {
            ControllerServiceEntity res = NifiHelper.getProcessGroupsApi().createControllerService(data.groupId, entity);
            //是否将控制器服务打开
            if (data.enabled) {
                BusinessResult<ControllerServiceEntity> model = updateDbControllerServiceState(res);
                if (model.success) {
                    return BusinessResult.of(true, "控制器服务创建成功，并开启运行", res);
                } else {
                    return BusinessResult.of(false, model.msg, null);
                }
            }
            return BusinessResult.of(true, "控制器服务创建成功", res);
        } catch (ApiException e) {
            log.error("创建连接池失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "创建连接池失败: " + e.getMessage(), null);
        }
    }


    /*
     *创建漏斗
     * */
    @Override
    public BusinessResult<FunnelEntity> createFunnel(FunnelDTO funnelDTO) {
        try {
            FunnelEntity funnelEntity = new FunnelEntity();
            RevisionDTO revision = new RevisionDTO();
            revision.setVersion(0L);
            funnelEntity.setRevision(revision);
            funnelEntity.setDisconnectedNodeAcknowledged(false);
            com.davis.client.model.FunnelDTO component = new com.davis.client.model.FunnelDTO();
            PositionDTO position = new PositionDTO();
            position.setX(500.00);
            position.setY(500.00);
            component.setPosition(position);
            funnelEntity.setComponent(component);
            FunnelEntity funnel = NifiHelper.getProcessGroupsApi().createFunnel(funnelDTO.groupId, funnelEntity);
            return BusinessResult.of(true, "漏斗创建成功", funnel);
        } catch (ApiException e) {
            log.error("创建漏斗失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "创建漏斗失败: " + e.getMessage(), null);
        }

    }

    @Override
    public void buildNifiGlobalVariable(Map<String, String> variable) {
        try {
            VariableRegistryEntity variableRegistry = NifiHelper.getProcessGroupsApi().getVariableRegistry(NifiConstants.ApiConstants.ROOT_NODE, true);
            VariableRegistryDTO registry = variableRegistry.getVariableRegistry();
            List<VariableEntity> variables = registry.getVariables();
            Iterator<Map.Entry<String, String>> externalStructureMap = variable.entrySet().iterator();

            while (externalStructureMap.hasNext()) {
                Map.Entry<String, String> next = externalStructureMap.next();
                String key = next.getKey();
                Boolean existent = false;
                for (int i = 0; i < variables.size(); i++) {
                    //判断有没有再保存创建
                    VariableDTO variableDTO = variables.get(i).getVariable();
                    String name = variableDTO.getName();
                    if (Objects.equals(key, name)) {
                        existent = true;
                    }
                }
                if (!existent) {
                    VariableRegistryEntity variableRegistryEntity = new VariableRegistryEntity();
                    VariableRegistryDTO registryDTO = new VariableRegistryDTO();
                    List<VariableEntity> variableEntities = new ArrayList<>();
                    VariableEntity variableEntity = new VariableEntity();
                    VariableDTO variableDTO = new VariableDTO();
                    variableDTO.setName(key);
                    variableDTO.setValue(variable.get(key));
                    variableEntity.setVariable(variableDTO);
                    variableEntities.add(variableEntity);
                    registryDTO.setVariables(variableEntities);
                    registryDTO.setProcessGroupId(variableRegistry.getVariableRegistry().getProcessGroupId());
                    variableRegistryEntity.setDisconnectedNodeAcknowledged(null);
                    variableRegistryEntity.setVariableRegistry(registryDTO);
                    RevisionDTO processGroupRevision = variableRegistry.getProcessGroupRevision();
                    variableRegistryEntity.setProcessGroupRevision(processGroupRevision);
                    System.out.println(JSON.toJSONString(variableRegistryEntity));
                    NifiHelper.getProcessGroupsApi().updateVariableRegistry(variableRegistry.getVariableRegistry().getProcessGroupId(), variableRegistryEntity);
                }
            }
        } catch (ApiException e) {
            log.error("查询全局变量失败." + e.getResponseBody());
        }
    }

    /*
     *创建notify组件
     * */
    @Override
    public BusinessResult<ProcessorEntity> createNotifyProcessor(BuildNotifyProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());

        Map<String, String> map = new HashMap<>(3);
        map.put("distributed-cache-service", data.distributedCacheService);
        map.put("release-signal-id", data.releaseSignalIdentifier);
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.NOTIFY.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }


    /*
     *创建wait组件
     * */
    @Override
    public BusinessResult<ProcessorEntity> createWaitProcessor(BuildWaitProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.EXPIRED.getName());

        Map<String, String> map = new HashMap<>(3);
        map.put("distributed-cache-service", data.distributedCacheService);
        map.put("release-signal-id", data.releaseSignalIdentifier);
        map.put("target-signal-count", data.targetSignalCount);
        map.put("expiration-duration", data.expirationDuration);

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.WAIT.getName());
        dto.setPosition(data.getPositionDTO());

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public List<String> getSqlForPgOds(DataAccessConfigDTO config) {
        List<String> SqlForPgOds = new ArrayList<>();
        String name = config.processorConfig.targetTableName;
        String deleteSql = assemblySql(config, SynchronousTypeEnum.TOPGODS, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName());
        config.processorConfig.targetTableName = "stg_" + name;
        String toOdaSql = assemblySql(config, SynchronousTypeEnum.TOPGODS, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName());
        SqlForPgOds.add(deleteSql);
        SqlForPgOds.add(toOdaSql);
        return SqlForPgOds;
    }

    @Override
    public String assemblySql(DataAccessConfigDTO config, SynchronousTypeEnum synchronousTypeEnum, String funcName) {
        TableBusinessDTO business = config.businessDTO;
        String targetTableName = config.processorConfig.targetTableName;
        String sql = "";
        sql += "call public." + funcName + "('";
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += "stg_" + targetTableName + "'";
                sql += ",'" + targetTableName + "'";
            } else {
                sql += targetTableName + "'";
                sql += ",'" + config.processorConfig.targetTableName.substring(4) + "'";
            }
        } else {
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += "stg_" + targetTableName + "'";
                sql += ",'ods_" + targetTableName + "'";
            } else {
                sql += targetTableName + "'";
                sql += ",'ods_" + targetTableName.substring(4) + "'";
            }
        }
        //同步方式
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        sql += ",'" + syncMode + "'";
        //主键
        sql += config.businessKeyAppend == null ? ",''" : ",'" + config.businessKeyAppend + "'";
        if (business == null) {
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += ",0,'',0,'','',0,'','',0,'')";
            } else if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName())) {
                sql += ",0,'',0,'','',0,'','',0,'')";
            }
        } else {
            //模式
            sql += "," + business.otherLogic;
            //年月日
            sql += (business.businessTimeFlag == null ? ",''" : ",'" + business.businessTimeFlag) + "'";
            //具体日期
            sql += "," + business.businessDate;
            //业务时间覆盖字段
            sql += (business.businessTimeField == null ? ",''" : ",'" + business.businessTimeField) + "'";
            //businessOperator
            String businessOperator = business.businessOperator;
            sql += (businessOperator == null ? ",''" : ",'" + businessOperator) + "'";
            //业务覆盖范围
            sql += "," + business.businessRange;
            //业务覆盖单位
            sql += (business.rangeDateUnit == null ? ",''" : ",'" + business.rangeDateUnit) + "'";
            //其他逻辑,逻辑符号
            String businessOperatorStandby = business.businessOperatorStandby;
            sql += (businessOperatorStandby == null ? ",''" : ",'" + businessOperatorStandby) + "'";
            //其他逻辑  业务覆盖范围
            sql += "," + business.businessRangeStandby;
            //其他逻辑  业务覆盖单位
            sql += (business.rangeDateUnitStandby == null ? ",''" : ",'" + business.rangeDateUnitStandby) + "')";
        }
        if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName())) {
            if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
                String s = associatedConditions(config);
                sql = sql.substring(0, sql.length() - 1);
                if (s == null && s.length() < 2) {
                    sql += ",'')";
                } else {
                    sql += ",'{\"AssociatedConditionDTO\":" + s + "}')";
                }
            } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                sql = sql.substring(0, sql.length() - 1);
                sql += ",'')";
            }
        }
        return sql;
    }

    @Override
    public void immediatelyStart(TableNifiSettingDTO tableNifiSettingDTO) {
        try {
            TableNifiSettingPO tableNifiSetting = tableNifiSettingService.getByTableId(tableNifiSettingDTO.tableAccessId, tableNifiSettingDTO.type);
            ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor(tableNifiSetting.publishKafkaProcessorId);
            enabledProcessor(tableNifiSetting.tableComponentId, processor);
            com.davis.client.model.ProcessorRunStatusEntity processorRunStatusEntity = new com.davis.client.model.ProcessorRunStatusEntity();
            processorRunStatusEntity.setDisconnectedNodeAcknowledged(false);
            processorRunStatusEntity.setRevision(processor.getRevision());
            processorRunStatusEntity.setState(com.davis.client.model.ProcessorRunStatusEntity.StateEnum.STOPPED);
            NifiHelper.getProcessorsApi().updateRunStatus(processor.getId(), processorRunStatusEntity);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public String associatedConditions(DataAccessConfigDTO config) {
        List<ModelPublishFieldDTO> fieldList = config.modelPublishFieldDTOList;
        String targetTableName = config.targetDsConfig.targetTableName;
        List<AssociatedConditionDTO> associatedConditionDTOS = new ArrayList<>();
        List<ModelPublishFieldDTO> collect1 = fieldList.stream().filter(e -> e.associateDimensionName != null).collect(Collectors.toList());
        List<ModelPublishFieldDTO> modelPublishFieldDTOS = collect1.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ModelPublishFieldDTO::getAssociateDimensionName))), ArrayList::new));
        modelPublishFieldDTOS.removeAll(Collections.singleton(null));
        if (modelPublishFieldDTOS.size() != 0) {
            int i = 0;

            for (ModelPublishFieldDTO modelPublishFieldDTO : modelPublishFieldDTOS) {
                AssociatedConditionDTO associatedConditionDTO = new AssociatedConditionDTO();
                //找到每个关联表关联的所有字段
                List<ModelPublishFieldDTO> collect = fieldList.stream().filter(e -> e.associateDimensionName != null && e.associateDimensionName.equals(modelPublishFieldDTO.associateDimensionName)).collect(Collectors.toList());
                //拼接语句,添加外键
                associatedConditionDTO.id = String.valueOf(i);
                associatedConditionDTO.associateDimensionName = modelPublishFieldDTO.associateDimensionName;
                String relevancy = "";
                for (ModelPublishFieldDTO modelPublishFieldDTO1 : collect) {
                    relevancy += targetTableName + "." + modelPublishFieldDTO1.fieldEnName + "=" + modelPublishFieldDTO1.associateDimensionName + "." + modelPublishFieldDTO1.associateDimensionFieldName + " and ";
                }
                associatedConditionDTO.relevancy = relevancy.substring(0, relevancy.length() - 4);
                associatedConditionDTOS.add(associatedConditionDTO);
                i++;
            }
        }
        return JSON.toJSONString(associatedConditionDTOS);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildGetFTPProcess(BuildGetFTPProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());

        Map<String, String> map = new HashMap<>(2);
        map.put("Hostname", data.hostname);
        map.put("Port", data.port);
        map.put("Username", data.username);
        map.put("Password", data.password);
        map.put("Remote Path", data.remotePath);
        map.put("File Filter Regex", data.fileFilterRegex);
        map.put("ftp-use-utf8", String.valueOf(data.ftpUseUtf8));
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.GETFTP.getName());
        dto.setPosition(data.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildFetchFTPProcess(BuildFetchFTPProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        autoRes.add(AutoEndBranchTypeEnum.COMMSFAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.NOTFOUND.getName());
        autoRes.add(AutoEndBranchTypeEnum.PERMISSIONDENIED.getName());

        Map<String, String> map = new HashMap<>(2);
        map.put("Hostname", data.hostname);
        map.put("Port", data.port);
        map.put("Username", data.username);
        map.put("Password", data.password);
        map.put("Remote File", data.remoteFile);
        map.put("ftp-use-utf8", String.valueOf(data.ftpUseUtf8));
        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.FETCHFTP.getName());
        dto.setPosition(data.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildConvertExcelToCSVProcess(BuildConvertExcelToCSVProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.ORIGINAL.getName());

        Map<String, String> map = new HashMap<>(2);
        //excel-extract-first-row
        map.put("excel-extract-first-row", String.valueOf(data.numberOfRowsToSkip));
        map.put("excel-format-values", String.valueOf(data.formatCellValues));
        //excel
        map.put("CSV Format", data.csvFormat);

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.ConvertExcelToCSV.getName());
        dto.setPosition(data.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

    @Override
    public BusinessResult<ProcessorEntity> buildConvertRecordProcess(BuildConvertRecordProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());

        Map<String, String> map = new HashMap<>(2);
        //excel-extract-first-row
        map.put("record-reader", data.recordReader);
        map.put("record-writer", data.recordWriter);

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);
        config.setComments(data.details);
        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setType(ProcessorTypeEnum.CONVERTRECORD.getName());
        dto.setPosition(data.positionDTO);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());

        return buildProcessor(data.groupId, entity, dto, config);
    }

}
