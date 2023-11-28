package com.fisk.dataservice.dto.ksfwebservice.notice;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @Author: wangjian
 * @Date: 2023-11-28
 * @Description:
 */
public class NoticeDetail {
    @JSONField(name = "EBELN")
    private String EBELN;

    @JSONField(name = "EINDT")
    private String EINDT;

    @JSONField(name = "POSNR")
    private String POSNR;

    @JSONField(name = "LTEXT")
    private String LTEXT;

    @JSONField(name = "PSTYV")
    private String PSTYV;

    @JSONField(name = "MATNR")
    private String MATNR;

    @JSONField(name = "MENGE")
    private String MENGE;

    @JSONField(name = "MEINS")
    private String MEINS;

    @JSONField(name = "NETPR")
    private String NETPR;

    @JSONField(name = "WERKS")
    private String WERKS;

    @JSONField(name = "NAME1")
    private String NAME1;

    @JSONField(name = "LGORT")
    private String LGORT;

    @JSONField(name = "LGOBE")
    private String LGOBE;

    @JSONField(name = "INSMK")
    private String INSMK;

    @JSONField(name = "CHARG")
    private String CHARG;

    public String getEBELN() {
        return EBELN;
    }

    public void setEBELN(String EBELN) {
        this.EBELN = EBELN;
    }

    public String getEINDT() {
        return EINDT;
    }

    public void setEINDT(String EINDT) {
        this.EINDT = EINDT;
    }

    public String getPOSNR() {
        return POSNR;
    }

    public void setPOSNR(String POSNR) {
        this.POSNR = POSNR;
    }

    public String getLTEXT() {
        return LTEXT;
    }

    public void setLTEXT(String LTEXT) {
        this.LTEXT = LTEXT;
    }

    public String getPSTYV() {
        return PSTYV;
    }

    public void setPSTYV(String PSTYV) {
        this.PSTYV = PSTYV;
    }

    public String getMATNR() {
        return MATNR;
    }

    public void setMATNR(String MATNR) {
        this.MATNR = MATNR;
    }

    public String getMENGE() {
        return MENGE;
    }

    public void setMENGE(String MENGE) {
        this.MENGE = MENGE;
    }

    public String getMEINS() {
        return MEINS;
    }

    public void setMEINS(String MEINS) {
        this.MEINS = MEINS;
    }

    public String getNETPR() {
        return NETPR;
    }

    public void setNETPR(String NETPR) {
        this.NETPR = NETPR;
    }

    public String getWERKS() {
        return WERKS;
    }

    public void setWERKS(String WERKS) {
        this.WERKS = WERKS;
    }

    public String getNAME1() {
        return NAME1;
    }

    public void setNAME1(String NAME1) {
        this.NAME1 = NAME1;
    }

    public String getLGORT() {
        return LGORT;
    }

    public void setLGORT(String LGORT) {
        this.LGORT = LGORT;
    }

    public String getLGOBE() {
        return LGOBE;
    }

    public void setLGOBE(String LGOBE) {
        this.LGOBE = LGOBE;
    }

    public String getINSMK() {
        return INSMK;
    }

    public void setINSMK(String INSMK) {
        this.INSMK = INSMK;
    }

    public String getCHARG() {
        return CHARG;
    }

    public void setCHARG(String CHARG) {
        this.CHARG = CHARG;
    }
}
