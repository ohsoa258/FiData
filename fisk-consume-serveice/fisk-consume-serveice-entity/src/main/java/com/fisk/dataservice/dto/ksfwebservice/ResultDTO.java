package com.fisk.dataservice.dto.ksfwebservice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Author: wangjian
 * @Date: 2023-10-24
 * @Description:
 */
@XmlRootElement(name = "Result")
public class ResultDTO {
    private String STATUS;
    private String INFOTEXT;
    private String MBLNR;

    public String getSTATUS() {
        return STATUS;
    }
    @XmlElement(name = "STATUS")
    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public String getINFOTEXT() {
        return INFOTEXT;
    }
    @XmlElement(name = "INFOTEXT")
    public void setINFOTEXT(String INFOTEXT) {
        this.INFOTEXT = INFOTEXT;
    }

    public String getMBLNR() {
        return MBLNR;
    }
    @XmlElement(name = "MBLNR")
    public void setMBLNR(String MBLNR) {
        this.MBLNR = MBLNR;
    }
}
