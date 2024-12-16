package com.fisk.common.core.constants;

/**
 * @author Lock
 * @version 1.3
 * @description redis存储token的key
 * @date 2022/2/21 12:03
 */
public class RedisTokenKey {
    /**
     * 数据服务token累加值
     */
    public static final Long DATA_SERVICE_TOKEN = 100000L;
    /**
     * 数据接入token累加值
     */
    public static final Long DATA_ACCESS_TOKEN = 200000L;


    /**
     * 数据校验token累加值
     */
    public static final Long DATA_CHECK_SERVICE_TOKEN = 300000L;

    /**
     * token累加值最大不超过400000
     */
    public static final Long TOKEN_MAX = 400000L;
}
