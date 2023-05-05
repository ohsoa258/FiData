package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionTimePeriodDTO {

    @ApiModelProperty(value = "维度表名称")
    public String dimensionTabName;

    @ApiModelProperty(value = "字段Id")
    public long fieldId;

    @ApiModelProperty(value = "维度属性字段")
    public String dimensionAttributeField;

}
