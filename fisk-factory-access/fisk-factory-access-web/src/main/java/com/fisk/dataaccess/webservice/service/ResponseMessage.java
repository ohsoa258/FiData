package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;

/**
 * 前置机定制接口
 * 通知单的返回类型
 */
@Data
public class ResponseMessage {

    /**
     * 执行情况
     */
    @ApiModelProperty(value = "执行情况")
    private String STATUS;

    /**
     * 文本信息
     */
    @ApiModelProperty(value = "文本信息")
    private String INFOTEXT;

    @XmlElement(name = "STATUS")
    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    @XmlElement(name = "INFOTEXT")
    public String getINFOTEXT() {
        return INFOTEXT;
    }

    public void setINFOTEXT(String INFOTEXT) {
        this.INFOTEXT = INFOTEXT;
    }
}
