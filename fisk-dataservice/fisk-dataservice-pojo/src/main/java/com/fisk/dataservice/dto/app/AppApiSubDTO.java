package com.fisk.dataservice.dto.app;

import com.fisk.dataservice.enums.ApiStateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 应用API DTO
 * @date 2022/1/6 14:51
 */
@Data
public class AppApiSubDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    @NotNull()
    public Integer apiId;

    /**
     * API状态 1启用、0禁用
     */
    @ApiModelProperty(value = "apiState")
    @NotNull
    public ApiStateTypeEnum apiState;
}
