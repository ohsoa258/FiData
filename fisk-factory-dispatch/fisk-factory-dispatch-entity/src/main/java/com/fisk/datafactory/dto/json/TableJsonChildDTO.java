package com.fisk.datafactory.dto.json;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/7 21:02
 */
@Data
public class TableJsonChildDTO {

    @ApiModelProperty(value = "表id")
    public long tableId;

    @ApiModelProperty(value = "表名称")
    public String tableName;


}
