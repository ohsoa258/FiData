package com.fisk.dataaccess.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableQueryDTO {

    @ApiModelProperty(value = "ods表名称")
    public String odsTableName;

    @ApiModelProperty(value = "stg表名称")
    public String stgTableName;

    @ApiModelProperty(value = "Id")
    public Integer id;

    @ApiModelProperty(value = "应用Id")
    public Integer appId;

}
