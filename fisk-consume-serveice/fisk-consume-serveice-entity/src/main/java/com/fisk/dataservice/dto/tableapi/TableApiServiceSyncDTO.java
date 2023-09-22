package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableApiServiceSyncDTO {

    /**
     * 表服务应用id
     */
    @ApiModelProperty(value = "api服务应用id")
    public Long appId;

    /**
     * 表服务表id
     */
    @ApiModelProperty(value = "api服务表id")
    public Long apiId;
}
