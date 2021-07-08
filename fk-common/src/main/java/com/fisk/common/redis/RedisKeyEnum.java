package com.fisk.common.redis;

import com.fisk.common.enums.BaseEnum;

/**
 * @author gy
 */

public enum RedisKeyEnum implements BaseEnum {
    /**
     * redis key 过期时间
     * value：过期时间
     * name：key
     */

    /**
     * 授权中心
     */
    AUTH_WHITELIST(0, "Auth:WhiteList"),
    AUTH_USERINFO(1800, "Auth:UserInfo"),
    CHARTVISUAL_DOWNLOAD_TOKEN(1800, "ChartVisual:DownLoad:Token");

    RedisKeyEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
