package com.fisk.datagovernance.vo.dataops;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 字段信息
 * @date 2022/4/22 13:02
 */
public class FieldVO {
    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;
}
