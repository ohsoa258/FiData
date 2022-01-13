package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 字段 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class FieldConfigDTO
{
    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String fieldName;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String fieldType;
}
