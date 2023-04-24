package com.fisk.datamodel.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataModelTableInfoDTO {

    @ApiModelProperty(value = "表Id")
    public Integer tableId;

    @ApiModelProperty(value = "业务域Id")
    public Integer businessAreaId;

    @ApiModelProperty(value = "表名")
    public String tableName;

    @ApiModelProperty(value = "olapTable")
    public Integer olapTable;

}
