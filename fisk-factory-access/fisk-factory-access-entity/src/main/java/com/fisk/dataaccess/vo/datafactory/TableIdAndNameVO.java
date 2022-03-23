package com.fisk.dataaccess.vo.datafactory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableIdAndNameVO {
    @ApiModelProperty(value = "物理表id", required = true)
    public long id;
    @ApiModelProperty(value = "物理表名称", required = true)
    public String name;
}
