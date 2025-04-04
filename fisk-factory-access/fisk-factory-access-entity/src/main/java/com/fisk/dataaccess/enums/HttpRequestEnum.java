package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/4/28 11:14
 */
public enum HttpRequestEnum implements BaseEnum {

    /**
     * 支持的所有数据源类型
     */

    /**
     * 查询类型
     */
    NULL(0, "没有请求方式"),
    GET(1, "get请求方式"),
    POST(2, "post请求方式");

    HttpRequestEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final int value;
    private final String name;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public static HttpRequestEnum getValue(String name) {
        HttpRequestEnum[] carTypeEnums = values();
        for (HttpRequestEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static HttpRequestEnum getName(int value) {
        HttpRequestEnum[] httpRequestEnums = values();
        for (HttpRequestEnum httpRequestEnum : httpRequestEnums) {
            int value1 = httpRequestEnum.value;
            if (value1 == value) {
                return httpRequestEnum;
            }
        }
        return null;
    }
}
