package com.fisk.task.utils;

import com.davis.client.ApiClient;
import com.davis.client.api.ControllerservicesApi;
import com.davis.client.api.ProcessgroupsApi;
import com.davis.client.api.ProcessorsApi;
import com.davis.client.model.ConnectableDTO;
import com.davis.client.model.RevisionDTO;
import com.fisk.common.constants.NifiConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * @author gy
 */
public class NifiHelper {

    /**
     * 获取process组的api操作类
     *
     * @return api操作类
     */
    public static ProcessgroupsApi getProcessGroupsApi() {
        return new ProcessgroupsApi(getApiClient());
    }

    /**
     * 获取controller-service的api操作类
     *
     * @return api操作类
     */
    public static ControllerservicesApi getControllerServicesApi() {
        return new ControllerservicesApi(getApiClient());
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
        apiClient.setBasePath(NifiConstants.ApiConstants.BASE_PATH);
        return apiClient;
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
