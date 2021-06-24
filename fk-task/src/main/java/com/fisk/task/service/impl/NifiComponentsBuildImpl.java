package com.fisk.task.service.impl;

import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.ControllerServiceTypeEnum;
import com.fisk.common.enums.task.nifi.ProcessorTypeEnum;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.entity.dto.nifi.*;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.utils.NifiHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gy
 */
@Slf4j
@Service
public class NifiComponentsBuildImpl implements INifiComponentsBuild {

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
            ProcessGroupEntity res = NifiHelper.getProcessGroupsApi().createProcessGroup(NifiHelper.getPid(dto.pid), entity);
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

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public ControllerServiceEntity getDbControllerService(String id) {
        try {
            return NifiHelper.getControllerServicesApi().getControllerService(id);
        } catch (ApiException e) {
            log.error("获取控制器服务失败，id【" + id + "】，【" + e.getResponseBody() + "】: ", e);
        }
        return null;
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
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildExecuteSqlProcess(BuildExecuteSqlProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());

        //组件属性
        Map<String, String> map = new HashMap<>(3);
        map.put("Database Connection Pooling Service", data.dbConnectionId);
        map.put("sql-pre-query", data.preSql);
        map.put("SQL select query", data.querySql);
        map.put("sql-post-query", data.postSql);

        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setSchedulingPeriod(data.scheduleExpression);
        config.setSchedulingStrategy(data.scheduleType.getName());
        config.setProperties(map);
        config.setAutoTerminatedRelationships(autoRes);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setDescription(data.details);
        dto.setType(ProcessorTypeEnum.ExecuteSQL.getName());
        dto.setConfig(config);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setPosition(data.getPositionDTO());
        entity.setComponent(dto);

        try {
            ProcessorEntity res = NifiHelper.getProcessGroupsApi().createProcessor(data.groupId, entity);
            return BusinessResult.of(true, "ExecuteSQL组件创建完成", res);
        } catch (ApiException e) {
            log.error("组件创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "ExecuteSQL组件创建失败" + e.getMessage(), null);
        }
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

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setDescription(data.details);
        dto.setType(ProcessorTypeEnum.ConvertAvroToJSON.getName());
        dto.setConfig(config);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setPosition(data.getPositionDTO());
        entity.setComponent(dto);

        try {
            ProcessorEntity res = NifiHelper.getProcessGroupsApi().createProcessor(data.groupId, entity);
            return BusinessResult.of(true, "ConvertAvroToJSON组件创建完成", res);
        } catch (ApiException e) {
            log.error("组件创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "ConvertAvroToJSON组件创建失败" + e.getMessage(), null);
        }
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

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setDescription(data.details);
        dto.setType(ProcessorTypeEnum.ConvertJSONToSQL.getName());
        dto.setConfig(config);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setPosition(data.getPositionDTO());
        entity.setComponent(dto);

        try {
            ProcessorEntity res = NifiHelper.getProcessGroupsApi().createProcessor(data.groupId, entity);
            return BusinessResult.of(true, "ConvertJSONToSQL组件创建完成", res);
        } catch (ApiException e) {
            log.error("组件创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "ConvertJSONToSQL组件创建失败" + e.getMessage(), null);
        }
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public BusinessResult<ProcessorEntity> buildPutSqlProcess(BuildPutSqlProcessorDTO data) {
        //流程分支，是否自动结束
        List<String> autoRes = new ArrayList<>();
        autoRes.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        autoRes.add(AutoEndBranchTypeEnum.FAILURE.getName());
        autoRes.add(AutoEndBranchTypeEnum.RETRY.getName());

        Map<String, String> map = new HashMap<>(1);
        map.put("JDBC Connection Pool", data.dbConnectionId);


        //组件配置信息
        ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setAutoTerminatedRelationships(autoRes);
        config.setProperties(map);

        //组件整体配置
        ProcessorDTO dto = new ProcessorDTO();
        dto.setName(data.name);
        dto.setDescription(data.details);
        dto.setType(ProcessorTypeEnum.PutSQL.getName());
        dto.setConfig(config);

        //组件传输对象
        ProcessorEntity entity = new ProcessorEntity();
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setPosition(data.getPositionDTO());
        entity.setComponent(dto);

        try {
            ProcessorEntity res = NifiHelper.getProcessGroupsApi().createProcessor(data.groupId, entity);
            return BusinessResult.of(true, "PutSQL组件创建完成", res);
        } catch (ApiException e) {
            log.error("组件创建失败，【" + e.getResponseBody() + "】: ", e);
            return BusinessResult.of(false, "PutSQL组件创建失败" + e.getMessage(), null);
        }
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

}
