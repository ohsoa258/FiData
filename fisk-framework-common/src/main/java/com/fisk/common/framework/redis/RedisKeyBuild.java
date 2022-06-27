package com.fisk.common.framework.redis;

import java.util.UUID;

/**
 * @author gy
 */
public class RedisKeyBuild {

    /**
     * @param id 用户id
     * @return redis key
     */
    public static String buildLoginUserInfo(long id) {
        return RedisKeyEnum.AUTH_USERINFO.getName() + ":" + id;
    }

    /**
     * @param id 用户id
     * @return redis key
     */
    public static String buildClientInfo(long id) {
        return RedisKeyEnum.AUTH_CLIENT_INFO.getName() + ":" + id;
    }

    /**
     * @return redis key
     */
    public static String buildDownLoadToken() {
        return RedisKeyEnum.CHARTVISUAL_DOWNLOAD_TOKEN.getName() + ":" + UUID.randomUUID().toString();
    }

    /**
     * 组装redis的key
     *
     * @param appId 应用id
     * @return redis key
     */
    public static String buildDataSoureKey(long appId) {
        return RedisKeyEnum.DATASOURCE_KEY.getName() + ":" + appId;
    }

    /**
     * 组装redis的key
     *
     * @param dataSourceId 数据源id
     * @return redis key
     */
    public static String buildFiDataStructureKey(String dataSourceId) {
        return RedisKeyEnum.FIDATA_STRUCTURE.getName() + ":" + dataSourceId;
    }

    /**
     * 组装redis的key
     *
     * @param id 管道id
     * @return redis key
     */
    public static String buildDispatchStructureKey(Long id) {
        return RedisKeyEnum.DISPATCH_STRUCTURE.getName() + ":" + id;
    }
}
