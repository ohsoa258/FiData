package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/17 14:05
 */
@Data
public class CopyApiDTO {

    @NotNull
    @ApiModelProperty(value = "当前api的appId", required = true)
    private Long appId;

    @NotNull
    @ApiModelProperty(value = "当前api的主键", required = true)
    private List<Long> apiIds;
}
