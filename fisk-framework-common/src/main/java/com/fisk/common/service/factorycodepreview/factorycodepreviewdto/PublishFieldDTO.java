package com.fisk.common.service.factorycodepreview.factorycodepreviewdto;

import lombok.Data;

/**
 * @author lishiji
 */
@Data
public class PublishFieldDTO {
    /**
     * 维度表字段id
     */
    public long fieldId;
    /**
     * 维度表英文字段名称
     */
    public String fieldEnName;
    /**
     * 维度表字段类型
     */
    public String fieldType;
    /**
     * 维度表字段长度
     */
    public int fieldLength;
    /**
     * 属性类型：0：维度属性
     */
    public int attributeType;
    /**
     * 是否业务主键 0:否 1:是
     */
    public int isPrimaryKey;
    /**
     * 源字段名称
     */
    public String sourceFieldName;
    /**
     * 关联维度id
     */
    public int associateDimensionId;
    /**
     * 关联维度表名称
     */
    public String associateDimensionName;
    /**
     * 关联维度表SQL脚本
     */
    public String associateDimensionSqlScript;
    /**
     * 关联维度字段id
     */
    public int associateDimensionFieldId;
    /**
     * 关联维度表字段名称
     */
    public String associateDimensionFieldName;
    /**
     * 字段精度
     */
    public Integer fieldPrecision;
    /**
     * 事实表：是否是业务覆盖标识 0:否 1:是
     */
    public int isBusinessKey;
}