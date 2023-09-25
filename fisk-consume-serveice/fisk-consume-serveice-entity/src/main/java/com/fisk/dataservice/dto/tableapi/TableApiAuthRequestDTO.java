package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-09-11
 * @Description:
 */
@Data
public class TableApiAuthRequestDTO {
    @ApiModelProperty(value = "id")
    private int id;

    @ApiModelProperty(value = "appId")
    private int appId;

    @ApiModelProperty(value = "请求参数key")
    private String parameterKey;

    @ApiModelProperty(value = "请求参数value")
    private String parameterValue;

}
