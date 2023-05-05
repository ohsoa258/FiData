package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildPutSqlProcessorDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "数据库连接Id")
    public String dbConnectionId;

    @ApiModelProperty(value = "sql声明")
    public String sqlStatement;
}
