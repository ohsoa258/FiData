package com.fisk.datamodel.dto.atomicindicator;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorPushDTO {
    /**
     * 字段类型：1关联维度
     */
    public int attributeType;
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
}
