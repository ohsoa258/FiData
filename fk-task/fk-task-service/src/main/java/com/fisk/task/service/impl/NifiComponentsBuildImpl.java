package com.fisk.task.service.impl;

import com.davis.client.ApiException;
import com.davis.client.api.ProcessorsApi;
import com.davis.client.model.*;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.ControllerServiceTypeEnum;
import com.fisk.common.enums.task.nifi.ProcessorTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.vo.ProcessGroupsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gy
 */
@Slf4j
@Service
public class NifiComponentsBuildImpl implements INifiComponentsBuild {

    @Resource
    RestTemplate httpClient;

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
    public BusinessResult<ControllerServiceEntity> buildAvroRecordSetWriterService(BaseProcessorDTO data) {
        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(5);


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

    //创建AvroReader
    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ControllerServiceEntity> buildAvroReaderService(BaseProcessorDTO data) {
        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        //对象的属性
        Map<String, String> map = new HashMap<>(5);


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
        Map<String, String> map = new HashMap<>(0);

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
    public BusinessResult<ProcessorEntity> buildReplaceTextProcess(BuildReplaceTextProcessorDTO data) {
        List<String> auto = new ArrayList<>(1);
        //流程分支，是否自动结束
        auto.add(AutoEndBranchTypeEnum.FAILURE.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(1);
        map.put("Replacement Value", data.replacementValue);

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
        map.put("esql-max-rows",data.MaxRowsPerFlowFile);
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
        map.put("SQL select query",buildCallDbProcedureProcessorDTO.executsql);
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
    public BusinessResult<ProcessorEntity> buildSqlParameterProcess(DataAccessConfigDTO dataAccessConfigDTO,BuildProcessEvaluateJsonPathDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.UNNMATCHED.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        Map<String, String> map = new HashMap<>();
        //添加字段
        map.put("Destination", "flowfile-attribute");
        List<TableFieldsDTO> tableFieldsList = dataAccessConfigDTO.targetDsConfig.tableFieldsList;
        for (TableFieldsDTO tableFieldsDTO:tableFieldsList) {
            map.put(tableFieldsDTO.fieldName.toLowerCase(),"$."+tableFieldsDTO.fieldName.toLowerCase());
        }
        map.put("fk_doris_increment_code","$.fk_doris_increment_code");
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
        String sql="insert into "+dataAccessConfigDTO.targetDsConfig.targetTableName.toLowerCase();
        String sqlfiled=" (";
        String sqlValue=" values (";
        //后面把fieldName替换成字段
        String sqlTemplate="${fieldName:isEmpty():ifElse('null',${fieldName:replace(\"'\",\"''\"):append(\"'\"):prepend(\"'\")})}";
        List<TableFieldsDTO> tableFieldsList = dataAccessConfigDTO.targetDsConfig.tableFieldsList;
        System.out.println("第二次拿到list长度"+tableFieldsList.size());
        for (TableFieldsDTO tableFieldsDTO:tableFieldsList) {
             sqlfiled+=tableFieldsDTO.fieldName.toLowerCase()+",";
            sqlValue+="${"+tableFieldsDTO.fieldName.toLowerCase()+":isEmpty():ifElse('null',${"+tableFieldsDTO.fieldName.toLowerCase()+":replace(\"'\",\"''\"):append(\"'\"):prepend(\"'\")})},";
        }
        sqlfiled+="fk_doris_increment_code) ";
        sqlValue+="${fk_doris_increment_code:isEmpty():ifElse('null',${fk_doris_increment_code:replace(\"'\",\"''\"):append(\"'\"):prepend(\"'\")})});";
        sql+=sqlfiled+sqlValue;
        map.put("Replacement Value",sql);
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
        map.put("Database Connection Pooling Service",executeSQLRecordDTO.databaseConnectionPoolingService);
        map.put("esqlrecord-record-writer",executeSQLRecordDTO.recordwriter);
        map.put("esql-max-rows",executeSQLRecordDTO.maxRowsPerFlowFile);
        map.put("esql-output-batch-size",executeSQLRecordDTO.outputBatchSize);
        map.put("esql-fetch-size",executeSQLRecordDTO.FetchSize);
        map.put("SQL select query",executeSQLRecordDTO.sqlSelectQuery);
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
        if(Objects.equals(putDatabaseRecordDTO.synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)){
            autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        }
        Map<String, String> map = new HashMap<>();
        map.put("put-db-record-record-reader",putDatabaseRecordDTO.recordReader);
        map.put("db-type",putDatabaseRecordDTO.databaseType);
        map.put("put-db-record-statement-type",putDatabaseRecordDTO.statementType);
        map.put("put-db-record-dcbp-service",putDatabaseRecordDTO.databaseConnectionPoolingService);
        map.put("put-db-record-table-name",putDatabaseRecordDTO.TableName);
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
        for (String selfDefinedParameter:data.selfDefinedParameter) {
            map.put(selfDefinedParameter,"$."+selfDefinedParameter);
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
    public BusinessResult<ProcessGroupsVO> getAllGroups(String groupId) {
        try {
            groupId = StringUtils.isEmpty(groupId) ? NifiConstants.ApiConstants.ROOT_NODE : groupId;
            String url = NifiConstants.ApiConstants.BASE_PATH + NifiConstants.ApiConstants.ALL_GROUP_RUN_STATUS.replace("{id}", groupId);
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

                String url = NifiConstants.ApiConstants.BASE_PATH + NifiConstants.ApiConstants.PROCESSOR_RUN_STATUS.replace("{id}", item.getId());
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

                String url = NifiConstants.ApiConstants.BASE_PATH + NifiConstants.ApiConstants.PROCESSOR_RUN_STATUS.replace("{id}", item.getId());
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
                if (entity.getComponent().getState() == ProcessorDTO.StateEnum.RUNNING) {
                    continue;
                }

                ProcessorEntity dto = new ProcessorEntity();
                dto.getComponent().getConfig().setSchedulingPeriod("");
                dto.getComponent().getConfig().setSchedulingStrategy("");
                HttpEntity<ProcessorEntity> request = new HttpEntity<>(dto, headers);

                String url = NifiConstants.ApiConstants.BASE_PATH + NifiConstants.ApiConstants.PUTPROCESS.replace("{id}", item.getId());
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

}
