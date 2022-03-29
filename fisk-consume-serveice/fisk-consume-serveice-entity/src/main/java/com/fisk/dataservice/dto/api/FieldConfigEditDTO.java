package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 编辑字段DTO
 * @date 2022/1/11 14:01
 */
@EqualsAndHashCode(callSuper = true)
public class FieldConfigEditDTO extends FieldConfigDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
