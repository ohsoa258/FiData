package com.fisk.task.service.impl;

import com.davis.client.ApiClient;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.utils.NifiHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gy
 */
@Slf4j
@Service
public class NifiComponentsBuildImpl implements INifiComponentsBuild {

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public ProcessGroupEntity buildProcessGroup(String name, String details, String pid, PositionDTO positionDTO) {
        ApiClient apiClient = NifiHelper.getApiClient();

        //请求实体
        ProcessGroupEntity entity = new ProcessGroupEntity();

        //group实体
        ProcessGroupDTO groupDTO = new ProcessGroupDTO();

        //group基础信息
        groupDTO.setName(name);
        groupDTO.setComments(details);
        groupDTO.setPosition(positionDTO);

        entity.setComponent(groupDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());

        try {
            ProcessGroupEntity res = NifiHelper.getProcessGroupsApi().createProcessGroup(NifiHelper.getPid(pid), entity);
            System.out.println(res.toString());
            return res;
        } catch (ApiException e) {
            log.error("分组创建失败，ex：", e);
        }
        return null;
    }

    @Override
    @TraceType(type = TraceTypeEnum.TASK_NIFI_ERROR)
    public ProcessGroupEntity getProcessGroupByPid(String pid) {
        try {
            return NifiHelper.getProcessGroupsApi().getProcessGroup(NifiHelper.getPid(pid));
        } catch (ApiException e) {
            log.error("查询分组失败，ex：", e);
        }
        return null;
    }

    //TODO: 没做完
    @Override
    public ControllerServiceEntity buildProcessControlService(String id, PositionDTO positionDTO) {
        //entity对象
        ControllerServiceEntity entity = new ControllerServiceEntity();

        Map<String, String> map = new HashMap<>(5);
        map.put("Database Connection URL", "jdbc:mysql://192.168.11.130:3306/dmp_datainput_db?serverTimezone=GMT%2B8&characterEncoding=UTF-8&useUnicode=true");
        map.put("Database Driver Class Name", "com.mysql.jdbc.Driver");
        map.put("database-driver-locations", "/opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar");
        map.put("Database User", "root");
        map.put("Password", "root123");

        //dto
        ControllerServiceDTO dto = new ControllerServiceDTO();
        dto.setType(NifiConstants.ApiConstants.DBCP_CONNECTION_POOL);
        dto.setName("test");
        dto.setProperties(map);

        entity.setPosition(positionDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        entity.setComponent(dto);

        try {
            ControllerServiceEntity res = NifiHelper.getProcessGroupsApi().createControllerService(id, entity);
        } catch (ApiException e) {
            log.error("创建连接池失败，ex：", e);
        }
        return null;
    }

}
