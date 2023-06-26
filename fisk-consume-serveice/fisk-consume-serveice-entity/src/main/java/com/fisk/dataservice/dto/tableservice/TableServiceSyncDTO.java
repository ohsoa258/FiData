package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableServiceSyncDTO {

    /**
     * 表服务应用id
     */
    @ApiModelProperty(value = "表服务应用id")
    public Long appId;

    /**
     * 表服务表id
     */
    @ApiModelProperty(value = "表服务表id")
    public Long tableId;
}
