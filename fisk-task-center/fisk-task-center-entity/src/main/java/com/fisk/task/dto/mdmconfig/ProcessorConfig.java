package com.fisk.task.dto.mdmconfig;

import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class ProcessorConfig {
    @ApiModelProperty(value = "目标表名")
    public String targetTableName;

    /**
     * 数据源执行的sql查询
     */
    @ApiModelProperty(value = "数据源执行的sql查询")
    public String sourceExecSqlQuery;

    @ApiModelProperty(value = "时间表达式")
    public String scheduleExpression;

    @ApiModelProperty(value = "计划类型")
    public SchedulingStrategyTypeEnum scheduleType;
}
