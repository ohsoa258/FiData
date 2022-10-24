package com.fisk.common.core.enums.task.nifi;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 */
public enum ControllerServiceTypeEnum implements BaseEnum {

    /**
     * 控制器服务类型ExecuteSQLRecord   Record Reader
     * org.apache.nifi.avro.AvroRecordSetWriter
     */
    DBCP_CONNECTION_POOL(0, "org.apache.nifi.dbcp.DBCPConnectionPool"),
    AVROREADER(1, "org.apache.nifi.avro.AvroReader"),
    AVRORECORDSETWRITER(2, "org.apache.nifi.avro.AvroRecordSetWriter"),
    REDISCONNECTIONPOOL(3,"org.apache.nifi.redis.service.RedisConnectionPoolService"),
    REDISDISTRIBUTEMAPCACHECLIENTSERVICE(4,"org.apache.nifi.redis.service.RedisDistributedMapCacheClientService"),
    CSVREADER(5,"org.apache.nifi.csv.CSVReader"),
    KEYTABCREDENTIALSSERVICE(6,"org.apache.nifi.kerberos.KeytabCredentialsService");


    ControllerServiceTypeEnum(int value, String name) {
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
