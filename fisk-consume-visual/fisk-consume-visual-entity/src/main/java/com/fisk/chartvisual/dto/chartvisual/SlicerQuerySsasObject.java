package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author JinXingWang
 */
public class SlicerQuerySsasObject {

    @ApiModelProperty(value = "Id")
    @NotNull
    public Integer id;

    @ApiModelProperty(value = "层次名称")
    @NotNull
    public String hierarchyName;
}
