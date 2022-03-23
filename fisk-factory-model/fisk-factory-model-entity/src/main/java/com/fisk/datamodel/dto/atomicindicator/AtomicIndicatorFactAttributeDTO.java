package com.fisk.datamodel.dto.atomicindicator;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorFactAttributeDTO {

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
     * 1：关联维度
     */
    public int attributeType;
    /**
     * 关联维度表名称
     */
    public String associateDimensionTable;

}
