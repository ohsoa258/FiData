package com.fisk.datagovernance.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 执行数据运维sql DTO
 * @date 2022/4/22 13:11
 */
public class ExecuteDataOpsSqlDTO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 执行的sql
     */
    @ApiModelProperty(value = "执行的sql")
    @NotNull()
    public String executeSql;
}
