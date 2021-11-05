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
    public String timePeriod;
}
