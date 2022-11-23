package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author Lock
 * @version 1.0
 * @description 数据源类型
 * @date 2021/12/28 11:02
 */
public enum DataSourceTypeEnum implements BaseEnum {
    /**
     * 查询类型
     */
    MYSQL(1, "mysql"),
    SQLSERVER(2, "sqlserver"),
    FTP(3, "ftp"),
    ORACLE(4, "oracle"),
    RestfulAPI(5, "RestfulAPI"),
    API(6, "api"),
    POSTGRESQL(7, "postgresql"),
    ORACLE_CDC(8, "oracle-cdc"),
    SFTP(9, "sftp");

    DataSourceTypeEnum(int value, String name) {
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

    public static DataSourceTypeEnum getValue(String name) {
        DataSourceTypeEnum[] enums = values();
        for (DataSourceTypeEnum typeEnum : enums) {
            String queryName = typeEnum.name;
            if (queryName.equals(name)) {
                return typeEnum;
            }
        }
        return null;
    }
}
