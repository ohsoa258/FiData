package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description api编辑 DTO
 * @date 2022/1/6 14:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiConfigEditDTO extends ApiConfigDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * api标识
     */
    @ApiModelProperty(value = "api标识")
    @NotNull()
    public String apiCode;
}
