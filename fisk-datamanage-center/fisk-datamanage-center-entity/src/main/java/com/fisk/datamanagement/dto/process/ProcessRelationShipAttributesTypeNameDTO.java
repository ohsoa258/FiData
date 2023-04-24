package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ProcessRelationShipAttributesTypeNameDTO {
    @ApiModelProperty(value = "属性名称")
    public String typeName;
}
