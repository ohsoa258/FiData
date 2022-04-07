package com.fisk.mdm.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/4/2 18:17
 */
@Data
public class UpdateEntityDTO extends EntityDTO {

    @NotNull
    @ApiModelProperty(value = "id",required = true)
    private Integer id;
}
