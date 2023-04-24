package com.fisk.task.dto.nifi;

import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildExecuteSqlProcessorDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "数据库连接Id")
    public String dbConnectionId;
    @ApiModelProperty(value = "计划类型")
    public SchedulingStrategyTypeEnum scheduleType;

    @ApiModelProperty(value = "时间表达式")
    public String scheduleExpression;
    @ApiModelProperty(value = "preSql")
    public String preSql;
    @ApiModelProperty(value = "querySql")
    public String querySql;
    @ApiModelProperty(value = "postSql")
    public String postSql;
    /**
     * 每次从结果集中提取的结果行数
     */
    @ApiModelProperty(value = "每次从结果集中提取的结果行数")
    public String fetchSize;
    /**
     * 规定多少记录一个FlowFile
     */
    @ApiModelProperty(value = "规定多少记录一个FlowFile")
    public String MaxRowsPerFlowFile;
    /**
     * 指定数量的流文件准备好传输
     */
    @ApiModelProperty(value = "指定数量的流文件准备好传输")
    public String outputBatchSize;
}
