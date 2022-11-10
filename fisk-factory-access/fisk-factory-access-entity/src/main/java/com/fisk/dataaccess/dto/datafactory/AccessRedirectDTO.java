package com.fisk.dataaccess.dto.datafactory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 2.5
 * @description 数据接入调转页面入参dto
 * @date 2022/6/14 14:50
 */
@Data
public class AccessRedirectDTO {

    @ApiModelProperty(value = "表的驱动类型(mysql、sqlserver、oracle、ftp、RestfulAPI、api)", required = true)
    @NotNull
    private String driveType;

    @ApiModelProperty(value = "应用主键id", required = true)
    @NotNull
    private Long appId;

    @ApiModelProperty(value = "物理表主键id")
    private Long tableId;

    @ApiModelProperty(value = "api主键id")
    private Long apiId;
}
