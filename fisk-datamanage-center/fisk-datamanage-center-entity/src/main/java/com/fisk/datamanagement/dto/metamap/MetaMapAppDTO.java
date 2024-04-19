package com.fisk.datamanagement.dto.metamap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MetaMapAppDTO {

    /**
     * 应用id/业务过程id
     */
    @ApiModelProperty(value = "应用id/业务过程id")
    private Integer appOrProcessId;

    /**
     * 应用名称/业务过程名称
     */
    @ApiModelProperty(value = "应用名称/业务过程名称")
    private String appOrProcessName;

    /**
     * 0 接入物理表
     * 1 数仓维度文件夹
     * 2 数仓业务过程（事实文件夹）
     */
    @ApiModelProperty(value = "0接入物理表 1数仓维度文件夹 2数仓业务过程（事实文件夹）")
    private Integer type;
}
