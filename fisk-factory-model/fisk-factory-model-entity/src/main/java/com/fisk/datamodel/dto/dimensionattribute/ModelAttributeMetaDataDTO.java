package com.fisk.datamodel.dto.dimensionattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ModelAttributeMetaDataDTO {
    /**
     * 字段id
     */
    @ApiModelProperty(value = "字段id")
    public String fieldId;
    /**
     * 维度字段名称
     */
    @ApiModelProperty(value = "维度字段名称")
    public String fieldEnName;

    /**
     * 维度字段类型
     */
    @ApiModelProperty(value = "维度字段类型")
    public String fieldType;

    /**
     * 维度字段长度
     */
    @ApiModelProperty(value = "维度字段长度")
    public int fieldLength;

    /**
     * 属性类型：1、维度属性 2、关联
     */
    @ApiModelProperty(value = "属性类型：1、维度属性 2、关联")
    public int attributeType;

    /**
     * 维度字段中文名称
     */
    @ApiModelProperty(value = "维度字段中文名称")
    public String fieldCnName;

    /**
     * 关联表名称
     */
    @ApiModelProperty(value = "关联表名称")
    public String associationTable;

    /**
     * 关联表字段名称
     */
    @ApiModelProperty(value = "关联表字段名称")
    public String associationField;

    /**
     * 字段来源id
     */
    @ApiModelProperty(value = "字段来源id")
    public int sourceFieldId;

    /**
     * 关联来源表字段id
     */
    @ApiModelProperty(value = "关联来源表字段id")
    public int associationSourceFieldId;

}
