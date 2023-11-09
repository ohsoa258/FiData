package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 前置机定制接口
 * 物料主数据的参数类
 */
@Data
public class KSF_Item {

    /**
     * 系统数据传入（接口结构最外层）
     */
    @ApiModelProperty(value = "系统数据传入（接口结构最外层）")
    private API_Message API_Message;

    /**
     * 业务数据行传入
     */
    @ApiModelProperty(value = "业务数据行传入")
    private List<ItemData> ItemData;

}
