package com.fisk.datamodel.dto.atomicindicator;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorPushDTO {
    /**
     * 字段类型：0 退化指标 1维度键 2 原子指标 3 派生指标
     */
    public int attributeType;
    /**
     * 退化维度字段名称
     */
    public String factFieldName;
    /**
     * 退化维度字段类型
     */
    public String factFieldType;
    /**
     * 退化维度字段类型长度
     */
    public int factFieldLength;

    /**
     * 关联维度表id
     */
    public long dimensionTableId;

    /**
     * 关联维度名称
     */
    public String dimensionTableName;

    /**
     * 原子指标名称
     */
    public String atomicIndicatorName;
    /**
     * 聚合字段
     */
    public String aggregatedField;
    /**
     * 聚合逻辑:sum、avg...
     */
    public String aggregationLogic;

    public long id;
    /**
     * 原子指标id
     */
    public int atomicId;

}
