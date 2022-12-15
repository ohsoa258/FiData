package com.fisk.common.framework.redis;

import com.fisk.common.core.enums.BaseEnum;

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
    AUTH_PUSH_DATA_LIST(0, "Auth:PushDataList"),
    AUTH_USERINFO(21600, "Auth:UserInfo"),
    // 客户端过期时间自定义,不使用当前设置的
    AUTH_CLIENT_INFO(0, "Auth:ClientInfo"),
    CHARTVISUAL_DOWNLOAD_TOKEN(1800, "ChartVisual:DownLoad:Token"),
    // 数据接入上游数据库结构key
    DATASOURCE_KEY(-1, "DataSourceMeta:appId"),
    // FiData数据结构(ods、dw、olap、mdm)
    FIDATA_STRUCTURE(-1, "FiData:Structure:DataSourceId"),
    // 数据调度管道数据结构
    DISPATCH_STRUCTURE(-1, "FiData:Dispatch:id"),
    // 数据调度管道数据结构--trace_id
    PIPEL_TRACE_ID(3000,"pipel:trace:id"),
    //数据调度管道数据结构--trace_id  (job层)
    PIPEL_JOB_TRACE_ID(3000,"pipel:JobTrace:id"),
    //数据调度管道数据结构--trace_id  (task层)
    PIPEL_TASK_TRACE_ID(3000,"pipel:TaskTrace:id"),
    //管道task
    PIPEL_TASK(3000, "pipel:task:id"),
    // FiData数据表结构(ods、dw、olap、mdm)
    FIDATA_TABLE_STRUCTURE(-1, "FiData:TableStructure:DataSourceId"),
    //redis token
    NIFI_TOKEN(3600,"nifiToken"),
    //delayedTask,检查延时队列失效
    DELAYED_TASK(3000,"delayed:task")
    ;

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
