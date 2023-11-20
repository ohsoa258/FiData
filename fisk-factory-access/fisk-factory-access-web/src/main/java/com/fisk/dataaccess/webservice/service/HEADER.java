package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * 前置机定制接口
 * 通知单-业务数据的父表类
 */
@Data
public class HEADER implements Serializable {
    /**
     * 收发类型
     */
    @ApiModelProperty(value = "收发类型")
    @JSONField(name = "BSART")
    private String BSART;

    /**
     * 单据号码
     */
    @ApiModelProperty(value = "单据号码")
    @JSONField(name = "EBELN")
    private String EBELN;

    /**
     * 仓库代码
     */
    @ApiModelProperty(value = "仓库代码")
    @JSONField(name = "LGPLA")
    private String LGPLA;

    /**
     * 货主代码
     */
    @ApiModelProperty(value = "货主代码")
    @JSONField(name = "VTXTK")
    private String VTXTK;

    /**
     * 供应商/送达方
     */
    @ApiModelProperty(value = "供应商/送达方")
    @JSONField(name = "LIFNR")
    private String LIFNR;

    /**
     * 供应商/送达方名称
     */
    @ApiModelProperty(value = "供应商/送达方名称")
    @JSONField(name = "NAME1")
    private String NAME1;

    /**
     * 售达方
     */
    @ApiModelProperty(value = "售达方")
    @JSONField(name = "KUNAG")
    private String KUNAG;

    /**
     * 承运商
     */
    @ApiModelProperty(value = "承运商")
    @JSONField(name = "KUNNR")
    private String KUNNR;

    /**
     * 承运商名称
     */
    @ApiModelProperty(value = "承运商名称")
    @JSONField(name = "NAME2")
    private String NAME2;

    /**
     * 抬头文本
     */
    @ApiModelProperty(value = "抬头文本")
    @JSONField(name = "HTEXT")
    private String HTEXT;

    /**
     * 抬头发货日期（ETD）
     */
    @ApiModelProperty(value = "抬头发货日期（ETD）")
    @JSONField(name = "BUDAT")
    private String BUDAT;

    @XmlElement(name = "BSART", nillable = false, required = true)
    public String getBSART() {
        return BSART;
    }

    public void setBSART(String BSART) {
        this.BSART = BSART;
    }

    @XmlElement(name = "EBELN", nillable = false, required = true)
    public String getEBELN() {
        return EBELN;
    }

    public void setEBELN(String EBELN) {
        this.EBELN = EBELN;
    }

    @XmlElement(name = "LGPLA", nillable = false, required = true)
    public String getLGPLA() {
        return LGPLA;
    }

    public void setLGPLA(String LGPLA) {
        this.LGPLA = LGPLA;
    }

    @XmlElement(name = "VTXTK", nillable = false, required = true)
    public String getVTXTK() {
        return VTXTK;
    }

    public void setVTXTK(String VTXTK) {
        this.VTXTK = VTXTK;
    }

    @XmlElement(name = "LIFNR", nillable = false, required = true)
    public String getLIFNR() {
        return LIFNR;
    }

    public void setLIFNR(String LIFNR) {
        this.LIFNR = LIFNR;
    }

    @XmlElement(name = "NAME1", nillable = false, required = true)
    public String getNAME1() {
        return NAME1;
    }

    public void setNAME1(String NAME1) {
        this.NAME1 = NAME1;
    }

    public String getKUNAG() {
        return KUNAG;
    }

    public void setKUNAG(String KUNAG) {
        this.KUNAG = KUNAG;
    }

    public String getKUNNR() {
        return KUNNR;
    }

    public void setKUNNR(String KUNNR) {
        this.KUNNR = KUNNR;
    }

    public String getNAME2() {
        return NAME2;
    }

    public void setNAME2(String NAME2) {
        this.NAME2 = NAME2;
    }

    public String getHTEXT() {
        return HTEXT;
    }

    public void setHTEXT(String HTEXT) {
        this.HTEXT = HTEXT;
    }

    public String getBUDAT() {
        return BUDAT;
    }

    public void setBUDAT(String BUDAT) {
        this.BUDAT = BUDAT;
    }
}
