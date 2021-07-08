package com.fisk.common.redis;

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
     * @return redis key
     */
    public static String buildDownLoadToken() {
        return RedisKeyEnum.CHARTVISUAL_DOWNLOAD_TOKEN.getName() + ":" + UUID.randomUUID().toString();
    }
}
