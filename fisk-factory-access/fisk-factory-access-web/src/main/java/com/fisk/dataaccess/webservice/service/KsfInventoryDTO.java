package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class KsfInventoryDTO {

    /**
     * 源系统ID
     */
    @ApiModelProperty(value = "源系统ID", required = true)
    @JSONField(name = "SourceSys")
    private String SourceSys;

    /**
     * 目标系统id
     */
    @ApiModelProperty(value = "目标系统id", required = true)
    @JSONField(name = "TargetSys")
    private String TargetSys;

    /**
     * 数据时间戳
     */
    @ApiModelProperty(value = "数据时间戳", required = true)
    @JSONField(name = "UpdateTime")
    private String UpdateTime;

    /**
     * 消息标识
     */
    @ApiModelProperty(value = "消息标识")
    @JSONField(name = "Guid")
    private String Guid;

    /**
     * 单个目标系统标识
     */
    @ApiModelProperty(value = "单个目标系统标识")
    @JSONField(name = "SingleTargetSys")
    private String SingleTargetSys;

    /**
     * 接口访问的验证信息
     */
    @ApiModelProperty(value = "接口访问的验证信息")
    @JSONField(name = "AppKey")
    private String AppKey;

    /**
     * 是否为网站测试接口
     */
    @ApiModelProperty(value = "是否为网站测试接口")
    @JSONField(name = "IsTest")
    private String IsTest;

    /**
     * 是否为网站手动消息
     */
    @ApiModelProperty(value = "是否为网站手动消息")
    @JSONField(name = "IsManualSend")
    private String IsManualSend;

    /**
     * 业务数据行传入
     */
    @ApiModelProperty(value = "业务数据行传入")
    private List<KSF_Inventory> KSF_Inventory;

}
