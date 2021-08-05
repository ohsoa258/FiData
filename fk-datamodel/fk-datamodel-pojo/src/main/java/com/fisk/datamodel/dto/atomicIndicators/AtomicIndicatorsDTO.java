package com.fisk.datamodel.dto.atomicIndicators;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsDTO {
    public int id;
    public int businessProcessId;
    public int factAttributeId;
    public String calculationLogic;
    public String indicatorsName;
    public String indicatorsDes;
}
