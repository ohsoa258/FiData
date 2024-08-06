package com.fisk.dataaccess.dto.tablefield;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-07-24
 * @Description:
 */
@Data
public class TableFieldDTO {
    /**
     * 字段id
     */
    @ApiModelProperty(value = "字段id", required = true)
    public String fieldId;

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id", required = true)
    public String tbId;
    /**
     * 字段名
     */
    @ApiModelProperty(value = "字段名", required = true)
    public String fieldName;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;
    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度", required = true)
    public int fieldLength = 0;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDes;

    /**
     * 字段精度
     */
    @ApiModelProperty(value = "字段精度", required = true)
    public Integer fieldPrecision;
}
