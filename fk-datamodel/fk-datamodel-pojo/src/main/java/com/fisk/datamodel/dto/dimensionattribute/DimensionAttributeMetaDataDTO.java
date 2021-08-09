package com.fisk.datamodel.dto.dimensionattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeMetaDataDTO {

    /**
     * 维度字段名称
     */
    public String dimensionFieldEnName;

    /**
     * 维度字段类型
     */
    public String dimensionFieldType;

    /**
     * 维度字段长度
     */
    public int dimensionFieldLength;

    /**
     * 属性类型：0、业务主键,1、关联维度,2、属性
     */
    public int attributeType;

}
