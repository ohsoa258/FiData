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
    // 数据视图数据库结构
    DATASOURCE_VIEW_KEY(-1, "DataSourceViewMeta:viewThemeId"),
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
    PIPEL_END_JOB_TRACE_ID(3000,"pipelend:JobTrace:id"),
    PIPEL_END_TASK_TRACE_ID(3000,"pipelend:TaskTrace:id"),
    //管道task
    PIPEL_TASK(3000, "pipel:task:id"),
    // FiData数据表结构(ods、dw、olap、mdm)
    FIDATA_TABLE_STRUCTURE(-1, "FiData:TableStructure:DataSourceId"),
    //redis token
    NIFI_TOKEN(3600,"nifiToken"),
    //delayedTask,检查延时队列失效
    DELAYED_TASK(3000,"delayed:task"),

    DISPATCH_RUN_ONCE(-1,"dispatch:runonce"),

    WEEK_MONITOR_ALL(87000,"monitor:week"),

    MONTH_MONITOR_ALL(87000,"monitor:month"),

    WEEK_MONITOR_SERVER(87000,"monitor:week:server"),

    MONTH_MONITOR_SERVER(87000,"monitor:month:server"),

    MONITOR_ACCESSLAKE_KAFKA(-1,"monitor:accesslake:kafka"),

    MONITOR_ACCESSLAKE_DORIS(-1,"monitor:accesslake:DORIS"),
    MONITOR_ACCESSLAKE_SQLSERVER(-1,"monitor:accesslake:SQLSERVER"),
    EMAIL_SEND_STATUS(-1,"monitor:emailSendStatus"),

    TABLE_KSF_WEB_SERVER_SYNC(-1,"Ksf:tabelWebServerSync"),
    DATA_SERVER_API_DATA(40,"DataServer:dataServerApiData"),

    DATA_SERVER_APP_ID(-1,"DataServer:dataServerAppId")
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
