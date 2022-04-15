package com.fisk.mdm.dto.attribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author ChenYa
 */
@Data
public class AttributeUpdateDTO extends AttributeDTO{
    @NotNull
    @ApiModelProperty(value = "id",required = true)
    private Integer id;
}
