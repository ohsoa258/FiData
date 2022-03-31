package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataAccessNumDTO {
    /**
     * 应用个数
     */
    @ApiModelProperty(value = "应用个数")
    public int num;
}
