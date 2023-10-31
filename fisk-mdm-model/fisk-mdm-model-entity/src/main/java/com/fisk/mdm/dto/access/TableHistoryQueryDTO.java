package com.fisk.mdm.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class TableHistoryQueryDTO {
    @ApiModelProperty(value = "表Id")
    public int tableId;
}
