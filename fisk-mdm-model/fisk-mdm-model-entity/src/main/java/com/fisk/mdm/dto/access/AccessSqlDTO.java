package com.fisk.mdm.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jianwenyang
 */
@Data
public class AccessSqlDTO {
    private long id;
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    private Integer dataSourceId;
    /**
     * 执行sql
     */
    @ApiModelProperty(value = "执行sql")
    private String sqlScript;
}
