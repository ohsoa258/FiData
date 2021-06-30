package com.fisk.task.dto.daconfig;

import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import lombok.Data;

/**
 * 数据接入配置项
 * @author gy
 */
@Data
public class DataAccessConfigDTO {
    public String appName;

    public String appDetails;

    /**
     * 是否需要创建新的项目
     */
    public boolean newApp;

    /**
     * 组件id
     */
    public String componentId;

    /**
     * 数据源jdbc连接
     */
    public DataSourceConfig sourceDsConfig;

    /**
     * 目标源jdbc连接
     */
    public DataSourceConfig targetDsConfig;

    public String targetTableName;

    /**
     * 数据源执行的sql查询
     */
    public String sourceExecSqlQuery;

    public String scheduleExpression;

    public SchedulingStrategyTypeEnum scheduleType;
}
