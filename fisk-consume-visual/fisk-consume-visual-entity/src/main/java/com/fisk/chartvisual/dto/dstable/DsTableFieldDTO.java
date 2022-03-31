package com.fisk.chartvisual.dto.dstable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/10 10:51
 */
@Data
public class DsTableFieldDTO {


    @ApiModelProperty(value = "源字段")
    private String sourceField;

    @ApiModelProperty(value = "目标字段名")
    private String targetField;

    @ApiModelProperty(value = "源字段类型")
    private String sourceFieldType;

    @ApiModelProperty(value = "目标字段类型")
    private String targetFieldType;

    @ApiModelProperty(value = "字段描述")
    private String describe;
}
