package com.fisk.dataaccess.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableInfoDTO {

    @ApiModelProperty(value = "表接入Id")
    public Integer tableAccessId;

    @ApiModelProperty(value = "应用ID")
    public Integer appId;

    @ApiModelProperty(value = "表名称")
    public String tableName;

    @ApiModelProperty(value = "olapTable")
    public Integer olapTable;


}
