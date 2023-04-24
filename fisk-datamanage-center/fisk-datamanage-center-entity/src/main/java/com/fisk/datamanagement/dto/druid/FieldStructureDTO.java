package com.fisk.datamanagement.dto.druid;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FieldStructureDTO {

    @ApiModelProperty(value = "字段名")
    public String fieldName;

    @ApiModelProperty(value = "别名")
    public boolean alias;

    @ApiModelProperty(value = "逻辑")
    public String logic;

    @ApiModelProperty(value = "源")
    public String source;
}
