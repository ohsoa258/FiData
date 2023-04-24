package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableServicePublishStatusDTO {

    /**
     * 表服务id
     */
    @ApiModelProperty(value = "表服务id")
    public int id;
    /**
     * 发布状态:0: 未发布  1: 发布成功  2: 发布失败
     */
    @ApiModelProperty(value = "发布状态")
    public int status;

}
