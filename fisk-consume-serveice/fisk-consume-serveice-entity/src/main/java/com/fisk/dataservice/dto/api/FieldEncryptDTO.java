package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-03-19
 * @Description:
 */
@Data
public class FieldEncryptDTO {
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
     * 字段排序
     */
    @ApiModelProperty(value = "字段排序")
    public int fieldSort;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    public String fieldDesc;

    /**
     * 加密
     */
    @ApiModelProperty(value = "加密")
    public int encrypt;
}
