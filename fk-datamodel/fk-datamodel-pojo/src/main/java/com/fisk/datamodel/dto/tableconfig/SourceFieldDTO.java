package com.fisk.datamodel.dto.tableconfig;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SourceFieldDTO {
    public long id;

    public String fieldName;

    public String fieldDes;

    public String fieldType;

    public int fieldLength;

    public int primaryKey;

    public int attributeType;
    /**
     * 是否关联维度
     */
    public boolean associatedDim;
    /**
     * 关联维度表id
     */
    public int associatedDimId;
    /**
     * 关联维度表名称
     */
    public String associatedDimName;
    /**
     * 关联维度字段表字段id
     */
    public int associatedDimAttributeId;
    /**
     * 关联维度字段表字段名称
     */
    public String associatedDimAttributeName;
    /**
     * 来源表
     */
    public String sourceTable;
    /**
     * 来源字段
     */
    public String sourceField;

}
