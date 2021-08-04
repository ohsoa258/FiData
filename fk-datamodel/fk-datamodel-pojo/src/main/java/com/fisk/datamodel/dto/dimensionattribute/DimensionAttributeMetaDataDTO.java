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

}
