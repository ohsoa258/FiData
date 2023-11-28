package com.fisk.dataservice.dto.ksfwebservice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @Author: wangjian
 * 通知单
 * @Date: 2023-10-24
 * @Description:
 */
@XmlRootElement(name = "KsfNotice")
public class KsfNoticeDTO {
    private System system;
    private List<Business> business;
    private List<BusinessDetail> businessDetails;

    public System getSystem() {
        return system;
    }

    @XmlElement(name = "System")
    public void setSystem(System system) {
        this.system = system;
    }

    public List<Business> getBusiness() {
        return business;
    }

    @XmlElement(name = "Business")
    public void setBusiness(List<Business> business) {
        this.business = business;
    }

    public List<BusinessDetail> getBusinessDetails() {
        return businessDetails;
    }

    @XmlElement(name = "BusinessDetail")
    public void setBusinessDetails(List<BusinessDetail> businessDetails) {
        this.businessDetails = businessDetails;
    }

    public static class System {
        private String SourceSys;
        private String TargetSys;
        private String UpdateTime;
        private String Guid;
        private String SingleTargetSys;
        private String AppKey;
        private String IsTest;
        private String IsManualSend;

        public String getSourceSys() {
            return SourceSys;
        }
        @XmlElement(name = "SourceSys")
        public void setSourceSys(String sourceSys) {
            SourceSys = sourceSys;
        }

        public String getTargetSys() {
            return TargetSys;
        }
        @XmlElement(name = "TargetSys")
        public void setTargetSys(String targetSys) {
            TargetSys = targetSys;
        }

        public String getUpdateTime() {
            return UpdateTime;
        }
        @XmlElement(name = "UpdateTime")
        public void setUpdateTime(String updateTime) {
            UpdateTime = updateTime;
        }

        public String getGuid() {
            return Guid;
        }
        @XmlElement(name = "Guid")
        public void setGuid(String guid) {
            Guid = guid;
        }

        public String getSingleTargetSys() {
            return SingleTargetSys;
        }
        @XmlElement(name = "SingleTargetSys")
        public void setSingleTargetSys(String singleTargetSys) {
            SingleTargetSys = singleTargetSys;
        }

        public String getAppKey() {
            return AppKey;
        }
        @XmlElement(name = "AppKey")
        public void setAppKey(String appKey) {
            AppKey = appKey;
        }

        public String getIsTest() {
            return IsTest;
        }
        @XmlElement(name = "IsTest")
        public void setIsTest(String isTest) {
            IsTest = isTest;
        }

        public String getIsManualSend() {
            return IsManualSend;
        }
        @XmlElement(name = "IsManualSend")
        public void setIsManualSend(String isManualSend) {
            IsManualSend = isManualSend;
        }
    }
    public static class Business {
        private String bsart;
        private String ebeln;
        private String sort1;
        private String lgpla;
        private String lifnr;
        private String name1;
        private String kunag;
        private String kunnr;
        private String name2;
        private String htext;
        private String budat;

        public String getBsart() {
            return bsart;
        }
        @XmlElement(name = "Bsart")
        public void setBsart(String bsart) {
            this.bsart = bsart;
        }

        public String getEbeln() {
            return ebeln;
        }
        @XmlElement(name = "Ebeln")
        public void setEbeln(String ebeln) {
            this.ebeln = ebeln;
        }

        public String getSort1() {
            return sort1;
        }
        @XmlElement(name = "Sort1")
        public void setSort1(String sort1) {
            this.sort1 = sort1;
        }

        public String getLgpla() {
            return lgpla;
        }
        @XmlElement(name = "Lgpla")
        public void setLgpla(String lgpla) {
            this.lgpla = lgpla;
        }

        public String getLifnr() {
            return lifnr;
        }
        @XmlElement(name = "Lifnr")
        public void setLifnr(String lifnr) {
            this.lifnr = lifnr;
        }

        public String getName1() {
            return name1;
        }
        @XmlElement(name = "Name1")
        public void setName1(String name1) {
            this.name1 = name1;
        }

        public String getKunag() {
            return kunag;
        }
        @XmlElement(name = "Kunag")
        public void setKunag(String kunag) {
            this.kunag = kunag;
        }

        public String getKunnr() {
            return kunnr;
        }
        @XmlElement(name = "Kunnr")
        public void setKunnr(String kunnr) {
            this.kunnr = kunnr;
        }

        public String getName2() {
            return name2;
        }
        @XmlElement(name = "Name2")
        public void setName2(String name2) {
            this.name2 = name2;
        }

        public String getHtext() {
            return htext;
        }
        @XmlElement(name = "Htext")
        public void setHtext(String htext) {
            this.htext = htext;
        }

        public String getBudat() {
            return budat;
        }
        @XmlElement(name = "Budat")
        public void setBudat(String budat) {
            this.budat = budat;
        }
    }

    public static class BusinessDetail {
        private String ebeln;
        private String eindt;
        private String posnr;
        private String ltext;
        private String pstyv;
        private String matnr;
        private String menge;
        private String meins;
        private String netpr;
        private String werks;
        private String name1;
        private String lgort;
        private String lgobe;
        private String insmk;
        private String charg;

        public String getEbeln() {
            return ebeln;
        }
        @XmlElement(name = "Ebeln")
        public void setEbeln(String ebeln) {
            this.ebeln = ebeln;
        }

        public String getEindt() {
            return eindt;
        }
        @XmlElement(name = "Eindt")
        public void setEindt(String eindt) {
            this.eindt = eindt;
        }

        public String getPosnr() {
            return posnr;
        }
        @XmlElement(name = "Posnr")
        public void setPosnr(String posnr) {
            this.posnr = posnr;
        }

        public String getLtext() {
            return ltext;
        }
        @XmlElement(name = "Ltext")
        public void setLtext(String ltext) {
            this.ltext = ltext;
        }

        public String getPstyv() {
            return pstyv;
        }
        @XmlElement(name = "Pstyv")
        public void setPstyv(String pstyv) {
            this.pstyv = pstyv;
        }

        public String getMatnr() {
            return matnr;
        }
        @XmlElement(name = "Matnr")
        public void setMatnr(String matnr) {
            this.matnr = matnr;
        }

        public String getMenge() {
            return menge;
        }
        @XmlElement(name = "Menge")
        public void setMenge(String menge) {
            this.menge = menge;
        }

        public String getMeins() {
            return meins;
        }
        @XmlElement(name = "Meins")
        public void setMeins(String meins) {
            this.meins = meins;
        }

        public String getNetpr() {
            return netpr;
        }
        @XmlElement(name = "Netpr")
        public void setNetpr(String netpr) {
            this.netpr = netpr;
        }

        public String getWerks() {
            return werks;
        }
        @XmlElement(name = "Werks")
        public void setWerks(String werks) {
            this.werks = werks;
        }

        public String getName1() {
            return name1;
        }
        @XmlElement(name = "Name1")
        public void setName1(String name1) {
            this.name1 = name1;
        }

        public String getLgort() {
            return lgort;
        }
        @XmlElement(name = "Lgort")
        public void setLgort(String lgort) {
            this.lgort = lgort;
        }

        public String getLgobe() {
            return lgobe;
        }
        @XmlElement(name = "Lgobe")
        public void setLgobe(String lgobe) {
            this.lgobe = lgobe;
        }

        public String getInsmk() {
            return insmk;
        }
        @XmlElement(name = "Insmk")
        public void setInsmk(String insmk) {
            this.insmk = insmk;
        }

        public String getCharg() {
            return charg;
        }
        @XmlElement(name = "Charg")
        public void setCharg(String charg) {
            this.charg = charg;
        }
    }
}
