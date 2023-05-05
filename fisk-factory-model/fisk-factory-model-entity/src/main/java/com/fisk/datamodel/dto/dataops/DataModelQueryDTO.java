package com.fisk.datamodel.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataModelQueryDTO {

    @ApiModelProperty(value = "ods表名")
    public String odsTableName;

    @ApiModelProperty(value = "stg表名")
    public String stgTableName;

    @ApiModelProperty(value = "Id")
    public Integer id;

    @ApiModelProperty(value = "业务域Id")
    public Integer businessAreaId;

    @ApiModelProperty(value = "表类型")
    public Integer tableType;

}
