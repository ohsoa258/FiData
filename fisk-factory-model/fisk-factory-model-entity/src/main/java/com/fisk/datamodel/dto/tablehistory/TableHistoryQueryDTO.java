package com.fisk.datamodel.dto.tablehistory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableHistoryQueryDTO {
    @ApiModelProperty(value = "表Id")
    public int tableId;
    @ApiModelProperty(value = "表类型")
    public int tableType;
}
