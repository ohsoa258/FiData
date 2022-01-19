package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 应用api订阅
 * @date 2022/1/17 13:26
 */
public class AppApiSubSaveDTO {
    /**
     * 应用api订阅dto
     */
    @ApiModelProperty(value = "应用api订阅dto")
    @NotNull()
    public List<AppApiSubDTO> dto;

    /**
     * 保存类型 1：api列表订阅保存、2：应用api列表订阅保存
     */
    @ApiModelProperty(value = "保存类型 1：api列表订阅保存、2：应用api列表订阅保存")
    @NotNull()
    public Integer saveType;
}
