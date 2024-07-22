package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据校验规则-检查条件
 * @date 2022/3/22 14:51
 */
@Data
public class DataCheckConditionDTO {
    /**
     * 数据校验检查条件表主键id
     */
    @ApiModelProperty(value = "数据校验检查条件表主键id")
    public int id;

    /**
     * 数据校验规则id
     */
    @ApiModelProperty(value = "数据校验规则id")
    public int ruleId;

    /**
     * 字段名称/字段Id
     */
    @ApiModelProperty(value = "字段名称/字段Id")
    public String fieldUnique;

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

    /**
     * 字段运算符
     */
    @ApiModelProperty(value = "字段运算符")
    public String fieldOperator;

    /**
     * 字段值
     */
    @ApiModelProperty(value = "字段值")
    public String fieldValue;

    /**
     * 字段关联条件：AND
     */
    @ApiModelProperty(value = "字段关联条件：AND")
    public String fieldRelationCondition;
}
