package com.fisk.dataservice.vo.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 字段 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class FieldConfigVO
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
    public int apiId;

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
     * 字段排序
     */
    @ApiModelProperty(value = "字段排序")
    public int fieldSort;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    public String fieldDesc;
}
