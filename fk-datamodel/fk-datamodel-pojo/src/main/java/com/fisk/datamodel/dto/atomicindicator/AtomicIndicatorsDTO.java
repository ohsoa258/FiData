package com.fisk.datamodel.dto.atomicindicator;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsDTO {
    public int id;
    public int businessId;
    public int factId;
    public int factAttributeId;
    public int indicatorsType;
    public String calculationLogic;
    public String indicatorsCnName;
    public String indicatorsName;
    public String indicatorsDes;
    public int businessLimitedId;
    public int atomicId;
    /**
     * 时间周期
     */
    public String timePeriod;
    /**
     * 派生指标类型：0基于原子指标、1基于指标公式
     */
    public int derivedIndicatorsType;
    /**
     * 指标公式
     */
    public String indicatorsFormula;
}
