package com.fisk.common.core.enums.dataservice;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 数据源类型
 *
 * @author
 */

public enum DataSourceTypeEnum implements BaseEnum {

    /**
     * 支持的所有数据源类型
     */
    MYSQL(0, "MYSQL", "com.mysql.jdbc.Driver", "opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar"),

    SQLSERVER(1, "SQLSERVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "opt/nifi/nifi-current/jdbcdriver/sqljdbc42.jar"),

    CUBE(2, "CUBE", "org.olap4j.driver.xmla.XmlaOlap4jDriver", ""),

    TABULAR(3, "TABULAR", "org.olap4j.driver.xmla.XmlaOlap4jDriver", ""),

    POSTGRESQL(4, "POSTGRESQL", "org.postgresql.Driver", "opt/nifi/nifi-current/jdbcdriver/postgresql-42.2.23.jar"),

    DORIS(5, "DORIS", "com.mysql.jdbc.Driver", "opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar"),

    ORACLE(6, "ORACLE", "oracle.jdbc.driver.OracleDriver", "opt/nifi/nifi-current/jdbcdriver/ojdbc7.jar"),

    REDSHIFT(7, "REDSHIFT", "", ""),

    RESTFULAPI(8,"RESTFULAPI","",""),

    API(9,"API","",""),

    FTP(10,"FTP","",""),

    SFTP(11,"SFTP","","");

    DataSourceTypeEnum(int value, String name, String driverName, String driverLocation) {
        this.driverName = driverName;
        this.name = name;
        this.value = value;
        this.driverLocation = driverLocation;
    }

    private final String driverName;
    private final String name;
    private final int value;
    private final String driverLocation;

    public String getDriverName() {
        return driverName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    public String getDriverLocation() {
        return driverLocation;
    }

    public static DataSourceTypeEnum getEnum(int value) {
        for (DataSourceTypeEnum e : DataSourceTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return DataSourceTypeEnum.MYSQL;
    }

    public static DataSourceTypeEnum getEnum(String name) {
        for (DataSourceTypeEnum e : DataSourceTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return DataSourceTypeEnum.MYSQL;
    }
}
