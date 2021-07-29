package com.fisk.datamodel.dto.fact;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDTO {

    /**
     * 表字段来源
     */
    public String tableSourceField;
    /**
     * 业务过程字段表中文字段名称
     */
    public String factFieldCnName;
    /**
     * 业务过程字段表字段类型
     */
    public String factFieldType;
    /**
     * 业务过程字段表字段长度
     */
    public int factFieldLength;
    /**
     * 业务过程字段表字段描述
     */
    public String factFieldDes;

    /**
     * 业务过程字段表英文字段名称
     */
    public String factFieldEnName;
    /**
     * 属性类型：1、事实属性,2、关联维度,3、度量
     */
    public int attributeType;
    /**
     * 关联业务过程字段表id
     */
    public int associateDimensionId;
    /**
     * 表来源
     */
    public String tableSource;

}
