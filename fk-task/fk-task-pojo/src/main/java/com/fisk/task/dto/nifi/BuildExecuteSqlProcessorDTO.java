package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class BuildExecuteSqlProcessorDTO {
    public String groupId;
    public String dbConnectionId;
    public String name;
    public String details;
    public PositionDTO positionDTO;

    public SchedulingStrategyTypeEnum scheduleType;
    public String scheduleExpression;

    public String preSql;
    public String querySql;
    public String postSql;
}
