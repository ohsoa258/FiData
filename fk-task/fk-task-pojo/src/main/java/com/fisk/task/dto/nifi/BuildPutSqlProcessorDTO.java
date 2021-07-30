package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildPutSqlProcessorDTO extends BaseProcessorDTO {

    public String dbConnectionId;

    public String sqlStatement;
}
