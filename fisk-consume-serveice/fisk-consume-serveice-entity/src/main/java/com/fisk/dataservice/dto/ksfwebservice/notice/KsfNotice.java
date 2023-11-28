package com.fisk.dataservice.dto.ksfwebservice.notice;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-28
 * @Description:
 */
public class KsfNotice {
    @JSONField(name = "BSART")
    private String BSART;

    @JSONField(name = "EBELN")
    private String EBELN;

    @JSONField(name = "LGPLA")
    private String LGPLA;

    @JSONField(name = "VTXTK")
    private String VTXTK;

    @JSONField(name = "LIFNR")
    private String LIFNR;

    @JSONField(name = "NAME1")
    private String NAME1;

    @JSONField(name = "KUNAG")
    private String KUNAG;

    @JSONField(name = "KUNNR")
    private String KUNNR;

    @JSONField(name = "NAME2")
    private String NAME2;

    @JSONField(name = "HTEXT")
    private String HTEXT;

    @JSONField(name = "BUDAT")
    private String BUDAT;

    @JSONField(name = "DETAIL")
    private List<NoticeDetail> DETAIL;

    public String getBSART() {
        return BSART;
    }

    public void setBSART(String BSART) {
        this.BSART = BSART;
    }

    public String getEBELN() {
        return EBELN;
    }

    public void setEBELN(String EBELN) {
        this.EBELN = EBELN;
    }

    public String getLGPLA() {
        return LGPLA;
    }

    public void setLGPLA(String LGPLA) {
        this.LGPLA = LGPLA;
    }

    public String getVTXTK() {
        return VTXTK;
    }

    public void setVTXTK(String VTXTK) {
        this.VTXTK = VTXTK;
    }

    public String getLIFNR() {
        return LIFNR;
    }

    public void setLIFNR(String LIFNR) {
        this.LIFNR = LIFNR;
    }

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

    public List<NoticeDetail> getDETAIL() {
        return DETAIL;
    }

    public void setDETAIL(List<NoticeDetail> DETAIL) {
        this.DETAIL = DETAIL;
    }
}
