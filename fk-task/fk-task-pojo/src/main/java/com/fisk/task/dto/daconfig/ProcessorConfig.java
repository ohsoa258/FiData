package com.fisk.task.dto.daconfig;

import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class ProcessorConfig {
    public String targetTableName;

    /**
     * 数据源执行的sql查询
     */
    public String sourceExecSqlQuery;

    public String scheduleExpression;

    public SchedulingStrategyTypeEnum scheduleType;
}
