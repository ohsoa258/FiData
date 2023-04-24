package com.fisk.task.dto.nifi;

import com.fisk.common.core.enums.task.nifi.StatementSqlTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildConvertJsonToSqlProcessorDTO extends BaseProcessorDTO {

    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "sql类型")
    public StatementSqlTypeEnum sqlType;

    @ApiModelProperty(value = "数据库联络Id")
    public String dbConnectionId;
}
