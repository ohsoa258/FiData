package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 表版本DTO
 * @date 2022/11/14 15:18
 */
@Data
public class TableVersionDTO {
    @ApiModelProperty(value = "参数key")
    @NotNull()
    public String keyStr;
}
