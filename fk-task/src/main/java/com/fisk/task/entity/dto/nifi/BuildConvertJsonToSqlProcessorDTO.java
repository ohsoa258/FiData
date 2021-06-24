package com.fisk.task.entity.dto.nifi;

import com.davis.client.model.PositionDTO;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class BuildConvertJsonToSqlProcessorDTO {
    public String groupId;
    public String name;
    public String details;

    public String tableName;
    public StatementSqlTypeEnum sqlType;
    public String dbConnectionId;

    public PositionDTO positionDTO;
}
