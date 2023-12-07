package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SyncOneTblForHudiDTO {

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    private Integer dbId;

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    private Integer appId;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    private String appName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    private List<String> tblNames;

}
