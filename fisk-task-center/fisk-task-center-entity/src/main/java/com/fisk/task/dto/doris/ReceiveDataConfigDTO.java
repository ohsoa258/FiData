package com.fisk.task.dto.doris;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Denny
 */
public class ReceiveDataConfigDTO extends MQBaseDTO {
    @ApiModelProperty(value = "id")
    public String id;
}
