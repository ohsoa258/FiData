package com.fisk.common.redis;

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
}
