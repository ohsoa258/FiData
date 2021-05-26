package com.fisk.common.enums.chartvisual;

/**
 * 数据源类型
 *
 * @author gy
 */

public enum DataSourceTypeEnum {

    /**
     * 支持的所有数据源类型
     */

    MYSQL(0),

    SQLSERVER(1);


    DataSourceTypeEnum(int code) {
        this.code = code;
    }

    private final int code;

    public int getCode() {
        return code;
    }
}
