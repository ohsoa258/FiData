package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * 前置机定制接口
 * 通知单-业务数据包含的父表和子表节点类
 */
@Data
public class Element implements Serializable {

    /**
     * 业务数据表头传入
     */
    @ApiModelProperty(value = "业务数据表头传入")
    private List<HEADER> HEADER;

    /**
     * 业务数据行传入
     */
    @ApiModelProperty(value = "业务数据行传入")
    private List<DETAIL> DETAIL;

    @XmlElement(name = "HEADER", nillable = false, required = true)
    public List<com.fisk.dataaccess.webservice.service.HEADER> getHEADER() {
        return HEADER;
    }

    public void setHEADER(List<com.fisk.dataaccess.webservice.service.HEADER> HEADER) {
        this.HEADER = HEADER;
    }

    @XmlElement(name = "DETAIL", nillable = false, required = true)
    public List<com.fisk.dataaccess.webservice.service.DETAIL> getDETAIL() {
        return DETAIL;
    }

    public void setDETAIL(List<com.fisk.dataaccess.webservice.service.DETAIL> DETAIL) {
        this.DETAIL = DETAIL;
    }
}
