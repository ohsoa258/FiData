package com.fisk.datagovernance.dto.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessAssemblyDTO {
    /**
     * 组件名称
     */
    @ApiModelProperty(value = "组件名称")
    public String assemblyName;

    /**
     * 组件描述
     */
    @ApiModelProperty(value = "组件描述")
    public String assemblyDescribe;

    /**
     * 组件ICON
     */
    @ApiModelProperty(value = "组件ICON")
    public String assemblyIcon;

    /**
     * 组件状态：1 启用 2 禁用
     */
    @ApiModelProperty(value = "组件状态：1 启用 2 禁用")
    public int assemblyState;
}
