package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/19 14:07
 */
@Data
public class ApiSelectChildDTO {

    @ApiModelProperty(value = "api的主键")
    public Long id;
    @ApiModelProperty(value = "api名称")
    public String apiName;
}
