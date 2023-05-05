package com.fisk.dataaccess.dto.v3;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableDTO {
    @ApiModelProperty(value = "类型")
    public int type;
    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;
}
