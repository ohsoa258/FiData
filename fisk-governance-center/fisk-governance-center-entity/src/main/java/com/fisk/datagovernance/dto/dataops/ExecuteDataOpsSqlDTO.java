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
     * 如存在 “” 特殊字符，需转义为\"test\"
     */
    @ApiModelProperty(value = "执行的sql，如存在双引号特殊字符，需使用反斜杠转义")
    @NotNull()
    public String executeSql;

    /**
     * 当前页，起始页为第一页
     */
    @ApiModelProperty(value = "当前页")
    public Integer current;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "size")
    public Integer size;
}
