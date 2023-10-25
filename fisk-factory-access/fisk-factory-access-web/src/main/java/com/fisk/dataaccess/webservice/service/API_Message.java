package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * 前置机定制接口
 * 四个接口的统一系统类
 */
@Data
public class API_Message implements Serializable {

    /**
     * 源系统ID
     */
    @ApiModelProperty(value = "源系统ID",required = true)
    private String SourceSys;

    /**
     * 目标系统id
     */
    @ApiModelProperty(value = "目标系统id",required = true)
    private String TargetSys;

    /**
     * 数据时间戳
     */
    @ApiModelProperty(value = "数据时间戳",required = true)
    private String UpdateTime;

    /**
     * 消息标识
     */
    @ApiModelProperty(value = "消息标识")
    private String Guid;

    /**
     * 单个目标系统标识
     */
    @ApiModelProperty(value = "单个目标系统标识")
    private String SingleTargetSys;

    /**
     * 接口访问的验证信息
     */
    @ApiModelProperty(value = "接口访问的验证信息")
    private String AppKey;

    /**
     * 是否为网站测试接口
     */
    @ApiModelProperty(value = "是否为网站测试接口")
    private String IsTest;

    /**
     * 是否为网站手动消息
     */
    @ApiModelProperty(value = "是否为网站手动消息")
    private String IsManualSend;

    @XmlElement(name = "SourceSys", nillable = false, required = true)
    public String getSourceSys() {
        return SourceSys;
    }

    public void setSourceSys(String sourceSys) {
        SourceSys = sourceSys;
    }

    @XmlElement(name = "TargetSys", nillable = false, required = true)
    public String getTargetSys() {
        return TargetSys;
    }

    public void setTargetSys(String targetSys) {
        TargetSys = targetSys;
    }

    @XmlElement(name = "UpdateTime", nillable = false, required = true)
    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String updateTime) {
        UpdateTime = updateTime;
    }

    @XmlElement(name = "Guid")
    public String getGuid() {
        return Guid;
    }

    public void setGuid(String guid) {
        Guid = guid;
    }

    @XmlElement(name = "SingleTargetSys")
    public String getSingleTargetSys() {
        return SingleTargetSys;
    }

    public void setSingleTargetSys(String singleTargetSys) {
        SingleTargetSys = singleTargetSys;
    }

    @XmlElement(name = "AppKey")
    public String getAppKey() {
        return AppKey;
    }

    public void setAppKey(String appKey) {
        AppKey = appKey;
    }

    @XmlElement(name = "IsTest")
    public String getIsTest() {
        return IsTest;
    }

    public void setIsTest(String isTest) {
        IsTest = isTest;
    }

    @XmlElement(name = "IsManualSend")
    public String getIsManualSend() {
        return IsManualSend;
    }

    public void setIsManualSend(String isManualSend) {
        IsManualSend = isManualSend;
    }
}
