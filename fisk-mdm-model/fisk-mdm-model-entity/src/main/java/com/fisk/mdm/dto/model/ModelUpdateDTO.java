package com.fisk.mdm.dto.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author ChenYa
 */
@Data
public class ModelUpdateDTO extends ModelDTO{
    @NotNull
    @ApiModelProperty(value = "id",required = true)
    private Integer id;
}
