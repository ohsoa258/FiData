package com.fisk.task.dto.nifi;

import com.fisk.common.core.enums.task.nifi.StatementSqlTypeEnum;
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
