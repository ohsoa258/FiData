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
    @NotNull()
    public Integer id;

    /**
     * 字段描述
     */
    @ApiModelProperty()
    @Length(min = 0, max = 255, message = "长度最多255")
    public String fieldDesc;
}
