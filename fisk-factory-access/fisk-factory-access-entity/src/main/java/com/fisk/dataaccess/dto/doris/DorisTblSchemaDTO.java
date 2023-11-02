package com.fisk.dataaccess.dto.doris;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DorisTblSchemaDTO {

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    private String type;

    /**
     * 可否为空
     */
    @ApiModelProperty(value = "可否为空")
    private String ifNull;

    /**
     * 是否为主键
     */
    @ApiModelProperty(value = "是否为主键")
    private String Key;

    /**
     * 字段的默认值
     */
    @ApiModelProperty(value = "字段的默认值")
    private String defaultValue;

    /**
     * 额外信息
     * 表示该字段的其他额外信息，例如自增（AUTO_INCREMENT）属性等。
     */
    @ApiModelProperty(value = "额外信息")
    private String extra;

}
