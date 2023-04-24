package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lishiji
 */
@Data
public class SqlCheckDTO {

    /**
     * sql语句
     */
    @ApiModelProperty(value = "sql语句")
    public String sql;

    /**
     * 数据库类型
     */
    @ApiModelProperty(value = "数据库类型")
    public String dbType;

}
