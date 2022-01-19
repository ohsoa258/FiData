package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description api预览
 * @date 2022/1/16 12:49
 */
public class ApiPreviewDTO {
    /**
     * api信息
     */
    @ApiModelProperty(value = "api信息")
    @NotNull()
    public ApiConfigEditDTO apiDTO;

    /**
     * 条件列表
     */
    @ApiModelProperty(value = "条件列表")
    public List<FilterConditionConfigEditDTO> whereDTO;

    /**
     * 参数列表
     */
    @ApiModelProperty(value = "参数列表")
    public List<ParmConfigEditDTO> parmDTO;
}
