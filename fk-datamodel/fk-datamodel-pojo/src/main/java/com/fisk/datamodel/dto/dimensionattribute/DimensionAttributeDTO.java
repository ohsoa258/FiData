package com.fisk.datamodel.dto.dimensionattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeDTO {
    /**
     * 表字段来源id
     */
    public int tableSourceFieldId;
    /**
     * 维度表中文字段名称
     */
    public String dimensionFieldCnName;
    /**
     * 维度表字段类型
     */
    public String dimensionFieldType;
    /**
     * 维度表字段长度
     */
    public int dimensionFieldLength;
    /**
     * 维度表字段描述
     */
    public String dimensionFieldDes;
    /**
     * 维度表英文字段名称
     */
    public String dimensionFieldEnName;
    /**
     * 属性类型：1、业务主键,2、关联维度,3、属性
     */
    public int attributeType;
    /**
     * 关联维度字段表id
     */
    public int associateDimensionId;
    /**
     * 关联维度id
     */
    public int associateId;

}
