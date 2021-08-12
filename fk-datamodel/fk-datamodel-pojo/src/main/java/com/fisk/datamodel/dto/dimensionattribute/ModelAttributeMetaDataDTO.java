package com.fisk.datamodel.dto.dimensionattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ModelAttributeMetaDataDTO {

    /**
     * 维度字段名称
     */
    public String fieldEnName;

    /**
     * 维度字段类型
     */
    public String fieldType;

    /**
     * 维度字段长度
     */
    public int fieldLength;

    /**
     * 属性类型：0、业务主键,1、关联维度,2、属性
     */
    public int attributeType;

}
