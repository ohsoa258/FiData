package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 过滤条件 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class FilterConditionConfigDTO
{
    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    public int apiId;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String fieldName;

    /**
     * 运算符
     * LIKE - 包含
     * EQU - 等于
     * NEQ - 不等于
     * LSS - 小于
     * LEQ - 小于或等于
     * GTR - 大于
     * GEQ - 大于或等于
     */
    @ApiModelProperty(value = "运算符")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String operator;

    /**
     * 字段值
     */
    @ApiModelProperty(value = "字段值")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String fieldValue;
}
