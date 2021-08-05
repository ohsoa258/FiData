package com.fisk.task.dto.nifi;

import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
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
}
