package com.fisk.dataservice.dto.ksfwebservice.item;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @Author: wangjian
 * @Date: 2023-11-28
 * @Description:
 */
public class KsfGoods {
    @JSONField(name = "MTART")
    private String MTART;
    @JSONField(name = "MTBEZ")
    private String MTBEZ;
    @JSONField(name = "MATNR")
    private String MATNR;
    @JSONField(name = "MAKTX")
    private String MAKTX;
    @JSONField(name = "NORMT")
    private String NORMT;
    @JSONField(name = "MEINS")
    private String MEINS;
    @JSONField(name = "VBAMG")
    private String VBAMG;

    public String getMTART() {
        return MTART;
    }

    public void setMTART(String MTART) {
        this.MTART = MTART;
    }

    public String getMTBEZ() {
        return MTBEZ;
    }

    public void setMTBEZ(String MTBEZ) {
        this.MTBEZ = MTBEZ;
    }

    public String getMATNR() {
        return MATNR;
    }

    public void setMATNR(String MATNR) {
        this.MATNR = MATNR;
    }

    public String getMAKTX() {
        return MAKTX;
    }

    public void setMAKTX(String MAKTX) {
        this.MAKTX = MAKTX;
    }

    public String getNORMT() {
        return NORMT;
    }

    public void setNORMT(String NORMT) {
        this.NORMT = NORMT;
    }

    public String getMEINS() {
        return MEINS;
    }

    public void setMEINS(String MEINS) {
        this.MEINS = MEINS;
    }

    public String getVBAMG() {
        return VBAMG;
    }

    public void setVBAMG(String VBAMG) {
        this.VBAMG = VBAMG;
    }
}
