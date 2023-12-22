package com.fisk.datagovernance.dto.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Data
public class TablesRowsDTO {
    @ApiModelProperty(value = "数据库类型")
    private String driverType;
    @ApiModelProperty(value = "数据库类型")
    private String dbName;
    @ApiModelProperty(value = "表名称")
    private String tableName;
    @ApiModelProperty(value = "行数")
    private Integer rows;
}
