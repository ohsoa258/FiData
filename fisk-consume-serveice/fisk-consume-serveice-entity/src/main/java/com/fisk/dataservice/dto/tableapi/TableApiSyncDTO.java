package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;

/**
 * @Author: wangjian
 * @Date: 2023-09-13
 * @Description:
 */
@Data
public class TableApiSyncDTO {
    @ApiModelProperty(value = "apiId")
    public Long apiId;
    @ApiModelProperty(value = "appId")
    public Long appId;
    @ApiModelProperty(value = "type")
    public Integer tableType;
    @ApiModelProperty(value = "checkByFieldMap")
    public HashMap<String, Object> checkByFieldMap;
}
