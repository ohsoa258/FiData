package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 前置机定制接口
 * 通知单的参数类
 */
@Data
public class KSF_Notice implements Serializable {

    /**
     * 系统数据传入（接口结构最外层）
     */
    @ApiModelProperty(value = "系统数据传入（接口结构最外层）")
    private API_Message API_Message;

    /**
     * 业务数据表头传入 & 业务数据行传入
     */
    @ApiModelProperty(value = "业务数据表头传入 & 业务数据行传入")
    private Elements Elements;
}
