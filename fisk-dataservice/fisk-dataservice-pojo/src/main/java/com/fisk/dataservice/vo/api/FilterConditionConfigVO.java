package com.fisk.dataservice.vo.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 过滤条件 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class FilterConditionConfigVO
{
    /**
     * Id
     */
    @ApiModelProperty(value = "主键")
    public int id;

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    public Integer apiId;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 运算符
     */
    @ApiModelProperty(value = "运算符")
    public String operator;

    /**
     * 字段值
     */
    @ApiModelProperty(value = "字段值")
    public String fieldValue;
}
