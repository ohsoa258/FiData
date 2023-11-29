package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * 前置机定制接口
 * 通知单-业务数据的子表类
 */
@Data
public class DETAIL implements Serializable {
    /**
     * 项目收货日期（ETA）
     */
    @ApiModelProperty(value = "项目收货日期（ETA）")
    @JSONField(name = "EINDT")
    private String EINDT;

    /**
     * 项目
     */
    @ApiModelProperty(value = "项目")
    @JSONField(name = "POSNR")
    private String POSNR;

    /**
     * 项目文本
     */
    @ApiModelProperty(value = "项目文本")
    @JSONField(name = "LTEXT")
    private String LTEXT;

    /**
     * 项目类别
     */
    @ApiModelProperty(value = "项目类别")
    @JSONField(name = "PSTYV")
    private String PSTYV;

    /**
     * 物料
     */
    @ApiModelProperty(value = "物料")
    @JSONField(name = "MATNR")
    private String MATNR;

    /**
     * 数量
     */
    @ApiModelProperty(value = "数量")
    @JSONField(name = "MENGE")
    private String MENGE;

    /**
     * 单据单位
     */
    @ApiModelProperty(value = "单据单位")
    @JSONField(name = "MEINS")
    private String MEINS;

    /**
     * 单价
     */
    @ApiModelProperty(value = "单价")
    @JSONField(name = "NETPR")
    private String NETPR;

    /**
     * 工厂
     */
    @ApiModelProperty(value = "工厂")
    @JSONField(name = "WERKS")
    private String WERKS;

    /**
     * 工厂名称
     */
    @ApiModelProperty(value = "工厂名称")
    @JSONField(name = "NAME1")
    private String NAME1;

    /**
     * 库存地点
     */
    @ApiModelProperty(value = "库存地点")
    @JSONField(name = "LGORT")
    private String LGORT;

    /**
     * 库存地点描述
     */
    @ApiModelProperty(value = "库存地点描述")
    @JSONField(name = "LGOBE")
    private String LGOBE;

    /**
     * 库存类型
     */
    @ApiModelProperty(value = "库存类型")
    @JSONField(name = "INSMK")
    private String INSMK;

    /**
     * 建议批号
     */
    @ApiModelProperty(value = "建议批号")
    @JSONField(name = "CHARG")
    private String CHARG;

    /**
     * 单据号码
     */
    @ApiModelProperty(value = "单据号码")
    @JSONField(name = "EBELN")
    private String EBELN;

    @XmlElement(name = "EBELN", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getEBELN() {
        return EBELN;
    }

    public void setEBELN(String EBELN) {
        this.EBELN = EBELN;
    }

    @XmlElement(name = "EINDT", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getEINDT() {
        return EINDT;
    }

    public void setEINDT(String EINDT) {
        this.EINDT = EINDT;
    }

    @XmlElement(name = "POSNR", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getPOSNR() {
        return POSNR;
    }

    public void setPOSNR(String POSNR) {
        this.POSNR = POSNR;
    }

    @XmlElement(name = "LTEXT", namespace = "http://tempuri.org/")
    public String getLTEXT() {
        return LTEXT;
    }

    public void setLTEXT(String LTEXT) {
        this.LTEXT = LTEXT;
    }

    @XmlElement(name = "PSTYV", namespace = "http://tempuri.org/")
    public String getPSTYV() {
        return PSTYV;
    }

    public void setPSTYV(String PSTYV) {
        this.PSTYV = PSTYV;
    }

    @XmlElement(name = "MATNR", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getMATNR() {
        return MATNR;
    }

    public void setMATNR(String MATNR) {
        this.MATNR = MATNR;
    }

    @XmlElement(name = "MENGE", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getMENGE() {
        return MENGE;
    }

    public void setMENGE(String MENGE) {
        this.MENGE = MENGE;
    }

    @XmlElement(name = "MEINS", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getMEINS() {
        return MEINS;
    }

    public void setMEINS(String MEINS) {
        this.MEINS = MEINS;
    }

    @XmlElement(name = "NETPR", namespace = "http://tempuri.org/")
    public String getNETPR() {
        return NETPR;
    }

    public void setNETPR(String NETPR) {
        this.NETPR = NETPR;
    }

    @XmlElement(name = "WERKS", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getWERKS() {
        return WERKS;
    }

    public void setWERKS(String WERKS) {
        this.WERKS = WERKS;
    }

    @XmlElement(name = "NAME1", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getNAME1() {
        return NAME1;
    }

    public void setNAME1(String NAME1) {
        this.NAME1 = NAME1;
    }

    @XmlElement(name = "LGORT", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getLGORT() {
        return LGORT;
    }

    public void setLGORT(String LGORT) {
        this.LGORT = LGORT;
    }

    @XmlElement(name = "LGOBE", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getLGOBE() {
        return LGOBE;
    }

    public void setLGOBE(String LGOBE) {
        this.LGOBE = LGOBE;
    }

    @XmlElement(name = "INSMK", nillable = false, required = true, namespace = "http://tempuri.org/")
    public String getINSMK() {
        return INSMK;
    }

    public void setINSMK(String INSMK) {
        this.INSMK = INSMK;
    }

    @XmlElement(name = "CHARG", namespace = "http://tempuri.org/")
    public String getCHARG() {
        return CHARG;
    }

    public void setCHARG(String CHARG) {
        this.CHARG = CHARG;
    }
}
