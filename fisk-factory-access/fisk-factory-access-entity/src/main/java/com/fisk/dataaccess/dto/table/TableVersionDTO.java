package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表版本DTO
 * @date 2022/11/3 15:06
 */
@Data
public class TableVersionDTO {
    /**
     * key
     */
    @ApiModelProperty(value = "key")
    public String keyStr;
}
