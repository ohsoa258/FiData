package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildConvertJsonToSqlProcessorDTO extends BaseProcessorDTO {

    public String tableName;
    public StatementSqlTypeEnum sqlType;
    public String dbConnectionId;
}
