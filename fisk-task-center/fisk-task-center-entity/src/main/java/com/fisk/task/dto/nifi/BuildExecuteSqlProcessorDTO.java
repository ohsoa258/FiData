package com.fisk.task.dto.nifi;

import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildExecuteSqlProcessorDTO extends BaseProcessorDTO {
    public String dbConnectionId;

    public SchedulingStrategyTypeEnum scheduleType;
    public String scheduleExpression;

    public String preSql;
    public String querySql;
    public String postSql;
    /**
     * 每次从结果集中提取的结果行数
     */
    public String fetchSize;
    /**
     * 规定多少记录一个FlowFile
     */
    public String MaxRowsPerFlowFile;
    /**
     * 指定数量的流文件准备好传输
     */
    public String outputBatchSize;
}
