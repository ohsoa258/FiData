package com.fisk.dataaccess.dto.datafactory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableIdAndNameDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "表名称")
    public String tableName;
}
