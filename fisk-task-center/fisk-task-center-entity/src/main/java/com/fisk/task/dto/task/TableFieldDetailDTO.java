package com.fisk.task.dto.task;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableFieldDetailDTO {

    @ApiModelProperty(value = "表模式")
    public String tableSchema;

    @ApiModelProperty(value = "表名称")
    public String tableName;
    @ApiModelProperty(value = "定序位置")
    public String ordinalPosition;

    @ApiModelProperty(value = "列名称")
    public String columnName;

    @ApiModelProperty(value = "数据类型")
    public String dataType;
    @ApiModelProperty(value = "字符最大长度")
    public String characterMaximumLength;
    @ApiModelProperty(value = "数值精度")
    public String numericPrecision;
    @ApiModelProperty(value = "存储的数字个数")
    public String numericScale;
    @ApiModelProperty(value = "是可以为空")
    public String isNullable;
    @ApiModelProperty(value = "列默认")
    public String columnDefault;

    @ApiModelProperty(value = "描述")
    public String description;
    @ApiModelProperty(value = "udt名称")
    public String udtName;

}
