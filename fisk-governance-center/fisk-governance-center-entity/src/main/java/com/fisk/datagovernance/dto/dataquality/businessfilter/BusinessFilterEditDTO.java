package com.fisk.datagovernance.dto.dataquality.businessfilter;

import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/3/24 13:48
 */
@EqualsAndHashCode(callSuper = true)
public class BusinessFilterEditDTO extends BusinessFilterDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
