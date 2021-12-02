package com.fisk.datamodel.dto.modelanalysispublish;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AnalysisPublishIndicatorDTO {
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
