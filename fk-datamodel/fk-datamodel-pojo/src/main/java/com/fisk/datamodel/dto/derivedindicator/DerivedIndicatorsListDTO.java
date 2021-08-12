package com.fisk.datamodel.dto.derivedindicator;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsListDTO extends DerivedIndicatorsDTO {

    /**
     * 原子指标名称
     */
    public String atomicName;

    /**
     * 业务限定名称
     */
    public String businessLimitedName;

}
