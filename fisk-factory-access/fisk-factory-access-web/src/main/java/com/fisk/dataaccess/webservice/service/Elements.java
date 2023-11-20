package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * 前置机定制接口
 * 通知单-业务数据包含的父表和子表节点类的父节点类
 */
@Data
public class Elements implements Serializable {

    /**
     * 业务数据表头传入 & 业务数据行传入
     */
    @ApiModelProperty(value = "业务数据表头传入 & 业务数据行传入")
    private Element Element;

    @XmlElement(name = "Element", nillable = false, required = true)
    public com.fisk.dataaccess.webservice.service.Element getElement() {
        return Element;
    }

    public void setElement(com.fisk.dataaccess.webservice.service.Element element) {
        Element = element;
    }
}
