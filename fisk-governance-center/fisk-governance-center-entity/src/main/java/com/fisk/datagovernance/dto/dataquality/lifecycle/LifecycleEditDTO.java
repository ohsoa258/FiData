package com.fisk.datagovernance.dto.dataquality.lifecycle;

import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/3/24 13:59
 */
@EqualsAndHashCode(callSuper = true)
public class LifecycleEditDTO extends LifecycleDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
