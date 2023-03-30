package com.fisk.datagovernance.vo.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessAssemblyVO {
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
}
