package com.fisk.datamodel.dto.derivedindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsListDTO extends DerivedIndicatorsDTO {

    /**
     * 原子指标名称
     */
    @ApiModelProperty(value = "原子指标名称")
    public String atomicName;

}
