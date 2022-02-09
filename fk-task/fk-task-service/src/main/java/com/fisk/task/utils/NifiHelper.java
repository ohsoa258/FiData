package com.fisk.task.utils;

import com.davis.client.ApiClient;
import com.davis.client.api.*;
import com.davis.client.model.ConnectableDTO;
import com.davis.client.model.RevisionDTO;
import com.fisk.common.constants.NifiConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author gy
 */
@Slf4j
@Component
public class NifiHelper {
    public static String basePath;
    @Value("${nifi.basePath}")
    public static void setBasePath(String basePath) {
        NifiHelper.basePath = basePath;
    }

    /**
     * 获取process组的api操作类
     *
     * @return api操作类
     */
//    public static ProcessgroupsApi getProcessGroupsApi() {
//        return new ProcessgroupsApi(getApiClient());
//    }
    public static ProcessGroupsApi getProcessGroupsApi() {
        return new ProcessGroupsApi(getApiClient());
    }

    /**
     * 获取controller-service的api操作类
     *
     * @return api操作类
     */
//    public static ControllerservicesApi getControllerServicesApi() {
//        return new ControllerservicesApi(getApiClient());
//    }
    public static ControllerServicesApi getControllerServicesApi() {
        return new ControllerServicesApi(getApiClient());
    }
    public void Test() {
        ControllerServicesApi controllerServicesApi = new ControllerServicesApi();
    }
    /**
     * 获取controller-service的api操作类
     *
     * @return api操作类
     */
    public static ProcessorsApi getProcessorsApi() {
        return new ProcessorsApi(getApiClient());
    }

    /**
     * 创建api连接
     *
     * @return api连接
     */
    public static ApiClient getApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(basePath);
        return apiClient;
    }

    public static OutputPortsApi getOutputPortsApi() {
        return new OutputPortsApi(getApiClient());
    }

    public static InputPortsApi getInputPortsApi() {
        return new InputPortsApi(getApiClient());
    }

    public static ConnectionsApi getConnectionsApi(){
        return new ConnectionsApi(getApiClient());
    }

    public static FunnelApi getFunnelApi() {
        return new FunnelApi(getApiClient());
    }

    public static FlowApi getFlowApi(){
        return new FlowApi(getApiClient());
    }

    /**
     * pid如果为null，返回默认值
     *
     * @return pid
     */
    public static String getPid(String pid) {
        return StringUtils.isEmpty(pid) ? NifiConstants.ApiConstants.ROOT_NODE : pid;
    }

    /**
     * 创建Revision对象(修改记录)
     *
     * @return Revision对象
     */
    public static RevisionDTO buildRevisionDTO() {
        RevisionDTO revisionDTO = new RevisionDTO();
        revisionDTO.setVersion(0L);
        revisionDTO.setClientId(UUID.randomUUID().toString());
        return revisionDTO;
    }

    /**
     * 创建连接器对象
     *
     * @param groupId groupid
     * @param id      id
     * @return 连接器对象
     */
    public static ConnectableDTO buildConnectableDTO(String groupId, String id) {
        ConnectableDTO dto = new ConnectableDTO();
        dto.setGroupId(groupId);
        dto.setId(id);
        dto.setType(ConnectableDTO.TypeEnum.PROCESSOR);
        return dto;
    }
}
