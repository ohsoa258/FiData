package com.fisk.datamodel.dto.derivedindicatorslimited;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsLimitedDTO {
    @ApiModelProperty(value = "业务限制Id")
    public int businessLimitedId;
    @ApiModelProperty(value = "指示器Id")
    public long indicatorsId;

    @ApiModelProperty(value = "条件")
    public String conditions;
}

