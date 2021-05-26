package com.fisk.common.enums.chartvisual;

import java.util.Arrays;

/**
 * 数据源类型
 *
 * @author gy
 */

public enum DataSourceTypeEnum {

    /**
     * 支持的所有数据源类型
     */

    MYSQL(0, "MYSQL"),

    SQLSERVER(1, "SQLSERVER");


    DataSourceTypeEnum(int code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    private final String msg;
    private final int code;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }


    public static DataSourceTypeEnum getByCode(Integer code) {
        return Arrays.stream(values()).filter(x -> code.equals(x.getCode())).findFirst().orElse(null);
    }
}
