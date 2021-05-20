package com.fisk.auth.constants;

/**
 * @author: Lock
 * @date: 2021/5/10 13:10
 */
public class RedisConstants {
    /**
     * 用户登录时记录TokenID的key前缀
     */
    public static final String JTI_KEY_PREFIX = "auth:login:uid:";
    /**
     * 用户登录时记录TokenID的超时时间
     */
    public static final Integer TOKEN_EXPIRE_SECONDS = 1800;
}
