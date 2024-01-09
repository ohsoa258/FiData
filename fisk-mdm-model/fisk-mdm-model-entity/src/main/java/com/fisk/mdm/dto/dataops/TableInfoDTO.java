package com.fisk.mdm.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class TableInfoDTO {

    @ApiModelProperty(value = "表接入Id")
    public Integer tableAccessId;

    @ApiModelProperty(value = "应用/模型ID")
    public Integer appId;

    @ApiModelProperty(value = "表名称")
    public String tableName;

    @ApiModelProperty(value = "olapTable")
    public Integer olapTable;


}
