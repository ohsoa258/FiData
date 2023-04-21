package com.fisk.datamodel.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class TableStructDTO {

    @ApiModelProperty(value = "字段名")
    public String fieldName;

    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    @ApiModelProperty(value = "摆脱")
    public Integer rid;
}
