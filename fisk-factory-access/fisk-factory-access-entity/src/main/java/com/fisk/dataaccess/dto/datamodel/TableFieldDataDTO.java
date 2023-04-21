package com.fisk.dataaccess.dto.datamodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableFieldDataDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    @ApiModelProperty(value = "类型")
    public int type;
}
