package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-09-25
 * @Description:
 */
@Data
public class ApiAttributeDTO {

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    public int apiId;

    /**
     * fieldId
     */
    @ApiModelProperty(value = "fieldId")
    public int fieldId;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    public String fieldDesc;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;
}
