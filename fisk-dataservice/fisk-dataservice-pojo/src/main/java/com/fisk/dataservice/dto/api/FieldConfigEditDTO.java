package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author Player
 * @version v1.0
 * @description TOOO
 * @date 2022/1/11 14:01
 */
public class FieldConfigEditDTO extends FieldConfigDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
