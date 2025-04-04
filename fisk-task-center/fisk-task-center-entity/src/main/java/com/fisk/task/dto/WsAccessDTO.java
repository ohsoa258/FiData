package com.fisk.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WsAccessDTO {

    /**
     * 数据接入 tb_api_config id
     */
    @ApiModelProperty(value = "数据接入 tb_api_config id")
    private Integer apiConfigId;

    /**
     * 批次号
     */
    @ApiModelProperty(value = "批次号")
    private String batchCode;

    /**
     * 源系统id
     */
    @ApiModelProperty(value = "源系统id")
    private String sourceSys;

    /**
     * 是否是确认单：0否 1是
     */
    @ApiModelProperty(value = "是否是确认单：0否 1是")
    private Integer isAcknowledgement;

}
