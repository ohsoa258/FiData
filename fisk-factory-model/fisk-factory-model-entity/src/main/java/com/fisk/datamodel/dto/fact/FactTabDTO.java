package com.fisk.datamodel.dto.fact;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactTabDTO {
    @ApiModelProperty(value = "id")
    public long id;
    @ApiModelProperty(value = "事实表中文名")
    public String factTableCnName;
}
