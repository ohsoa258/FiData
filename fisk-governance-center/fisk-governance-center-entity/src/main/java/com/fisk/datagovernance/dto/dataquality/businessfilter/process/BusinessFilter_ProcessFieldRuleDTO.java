package com.fisk.datagovernance.dto.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessFieldRuleDTO {
    /**
     * 自定义描述
     */
    @ApiModelProperty(value = "自定义描述")
    public String customDescribe;

    /**
     * 字段规则排序
     */
    @ApiModelProperty(value = "字段规则排序")
    public String fieldRuleSort;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段运算符
     */
    @ApiModelProperty(value = "字段运算符")
    public String fieldOperator;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    /**
     * 字段值
     */
    @ApiModelProperty(value = "字段值")
    public String fieldValue;
}
