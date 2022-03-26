package com.fisk.datagovernance.dto.dataquality.emailserver;

import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/3/24 13:56
 */
@EqualsAndHashCode(callSuper = true)
public class EmailServerEditDTO extends EmailServerDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
