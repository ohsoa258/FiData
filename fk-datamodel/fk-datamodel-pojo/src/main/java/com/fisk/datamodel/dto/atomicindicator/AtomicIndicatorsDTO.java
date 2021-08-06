package com.fisk.datamodel.dto.atomicindicator;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsDTO {
    public int id;
    public int factAttributeId;
    public String calculationLogic;
    public String indicatorsName;
    public String indicatorsDes;
}
