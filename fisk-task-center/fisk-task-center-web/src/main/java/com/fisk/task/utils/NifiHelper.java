package com.fisk.task.utils;

import com.davis.client.ApiClient;
import com.davis.client.ApiException;
import com.davis.client.api.*;
import com.davis.client.model.ConnectableDTO;
import com.davis.client.model.RevisionDTO;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author gy
 */
@Slf4j
@Component
public class NifiHelper implements ApplicationContextAware {
    public static String basePath;
    private static String nifiUsername;
    private static String nifiPassword;

    @Value("${nifi.basePath}")
    public void setBasePath(String basePath) {
        NifiHelper.basePath = basePath;
    }

    @Value("${nifi.username}")
    public void setNifiUsername(String nifiUsername) {
        NifiHelper.nifiUsername = nifiUsername;
    }

    @Value("${nifi.password}")
    public void setNifiPassword(String nifiPassword) {
        NifiHelper.nifiPassword = nifiPassword;
    }

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        NifiHelper.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext != null ? applicationContext.getBean(clazz) : null;
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


    public static RemoteProcessGroupsApi getRemoteProcessGroupsApi() {
        return new RemoteProcessGroupsApi(getApiClient());
    }

    /**
     * 获取controller-service的api操作类
     *
     * @return api操作类
     */

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

    public static AccessApi getAccessApi() {
        return new AccessApi(getApiClient());
    }

    public static AccessApi getAccessApiForToken() {
        return new AccessApi(getApiClientForToken());
    }

    /**
     * 创建api连接
     *
     * @return api连接
     */
    public static ApiClient getApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(basePath);
        String nifiToken = getNifiToken();
        apiClient.setVerifyingSsl(false);
        //apiClient.addDefaultHeader("Authorization", "Bearer eyJraWQiOiJkMmVlOGI1Ni1hZjNjLTRkNTEtODI1Yi0zYmFjZDc5ZWE5NzIiLCJhbGciOiJQUzUxMiJ9.eyJzdWIiOiJmaXNrIiwiYXVkIjoiU2luZ2xlVXNlckxvZ2luSWRlbnRpdHlQcm92aWRlciIsIm5iZiI6MTY2NjMzMTkyOCwiaXNzIjoiU2luZ2xlVXNlckxvZ2luSWRlbnRpdHlQcm92aWRlciIsInByZWZlcnJlZF91c2VybmFtZSI6ImZpc2siLCJleHAiOjE2NjYzNjA3MjgsImlhdCI6MTY2NjMzMTkyOCwianRpIjoiN2U3Y2M4ZmEtNGMzNy00YTljLWFjN2ItYjlhZmZlNjczMjRmIn0.gbLWs-TjZZKNUYPNKuev9YBVbtOAnDqYW_IbGFjgQJxqPz7cXPFDBhNiy3nB5inLSl-n5hggSVITQeIiIs8fvUoNeWbiNSDwRAaDe6E4EONm0SQjKxRzHo6eDVE8XITkTThTv83FCrnxHGkuBG_xLYPSrNmKH1yxKh5mIQXSKveSBj3itMN8jBbJMuX6vNcSuOrUX-r9pdgsXP-adTxMwViLjoo695EXWcAX4UEb8mwDNLeGktewxB0Np4BbNFjl5lUBCTRURar36eg2ETJ3RlBX1NCZakQPv0kBEjqUynhrXHaYXQ_qFCjwQSboCZX2W54FveCWeNOjmvVHGd2eUbipFgj2DfyRQFv6aUJ_onTykQtv-d8LzKwWzsatQZgrZ5kVOlkt0AU-cw2emKm9fIpInkqlvkm15GsH379Cs_d17vDLSh5Xus96c24gRauOZ-8couHtkYKfmOWyUsNSLf7AInWM7IpnUzaA6WOiBZX8GLO95ADiQYKdvFmVOjd03iRpQ3j4lmJtMFteocKeAFFpIwl_tTaETAsNneGObmd8AN5aPZvfsQpJgwZl7VUX6gF8bAABXR5hfuHMrH_t8H7eRIAbbEiD4PAunO7VfTfsmCuaBr_W4K1nlyNIwbtpWxISkmGtPp6RkihWgjgMY9AqSIdAKz7HPZpIerAZPJE");
        apiClient.addDefaultHeader("Authorization", nifiToken);
        return apiClient;
    }

    public static ApiClient getApiClientForToken() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(basePath);
        apiClient.setVerifyingSsl(false);
        return apiClient;
    }

    public static String getNifiToken() {
        String nifiToken = "";
        try {
            RedisUtil redisUtil = NifiHelper.getBean(RedisUtil.class);
            boolean hasKey = redisUtil.hasKey(RedisKeyEnum.NIFI_TOKEN.getName());
            if (hasKey) {
                nifiToken = redisUtil.get(RedisKeyEnum.NIFI_TOKEN.getName()).toString();
            } else {
                nifiToken = "Bearer "+NifiHelper.getAccessApiForToken().createAccessToken(nifiUsername, nifiPassword);
                redisUtil.set(RedisKeyEnum.NIFI_TOKEN.getName(), nifiToken, RedisKeyEnum.NIFI_TOKEN.getValue());
            }


        } catch (ApiException e) {
            e.printStackTrace();
        }
        return nifiToken;
    }

    public static OutputPortsApi getOutputPortsApi() {
        return new OutputPortsApi(getApiClient());
    }

    public static InputPortsApi getInputPortsApi() {
        return new InputPortsApi(getApiClient());
    }

    public static ConnectionsApi getConnectionsApi() {
        return new ConnectionsApi(getApiClient());
    }

    public static FunnelApi getFunnelApi() {
        return new FunnelApi(getApiClient());
    }

    public static FlowApi getFlowApi() {
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
