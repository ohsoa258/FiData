package com.fisk.datamodel.dto.fact;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactScreenDropDTO {
    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "事实表中文名称")
    public String factTableCnName;
}
