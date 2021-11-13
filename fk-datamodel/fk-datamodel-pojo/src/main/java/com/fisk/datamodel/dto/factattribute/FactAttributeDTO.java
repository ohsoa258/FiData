package com.fisk.datamodel.dto.factattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDTO {
    public long id;
    /**
     * 表字段来源id
     */
    //public int tableSourceFieldId;
    /**
     * 维度表中文字段名称
     */
    public String factFieldCnName;
    /**
     * 维度表字段类型
     */
    public String factFieldType;
    /**
     * 维度表字段长度
     */
    public int factFieldLength;
    /**
     * 维度表字段描述
     */
    public String factFieldDes;

    /**
     * 维度表英文字段名称
     */
    public String factFieldEnName;
    /**
     * 属性类型：1、事实属性,2、度量
     */
    public int attributeType;
    /**
     * 关联维度表id
     */
    public int associateDimensionId;
    /**
     * 关联维度字段id
     */
    public int associateDimensionFieldId;

}
