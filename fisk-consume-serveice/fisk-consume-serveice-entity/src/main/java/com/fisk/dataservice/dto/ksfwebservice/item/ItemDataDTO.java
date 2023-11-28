package com.fisk.dataservice.dto.ksfwebservice.item;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @Author: wangjian
 * 物料主数据
 * @Date: 2023-10-24
 * @Description:
 */
@XmlRootElement(name = "ItemData")
public class ItemDataDTO {

    private List<Business> businesses;
    private System system;

    public List<Business> getBusinesses() {
        return businesses;
    }

    @XmlElement(name = "Business")
    public void setBusinesses(List<Business> businesses) {
        this.businesses = businesses;
    }

    public System getSystem() {
        return system;
    }

    @XmlElement(name = "System")
    public void setSystem(System system) {
        this.system = system;
    }

    public static class Business {

        private String MTART;
        private String MTBEZ;
        private String MATNR;
        private String MAKTX;
        private String NORMT;
        private String LVORM;
        private String MEINS;
        private String MSEHT;
        private String MEINS1;
        private String MSEHT1;
        private String RATE1;
        private String MEINS2;
        private String MSEHT2;
        private String RATE2;
        private String MEINS3;
        private String MSEHT3;
        private String RATE3;
        private String MEINS4;
        private String MSEHT4;
        private String RATE4;
        private String MEINS5;
        private String MSEHT5;
        private String RATE5;
        private String MATKL;
        private String WGBEZ;
        private String MSTAE;
        private String LABOR;
        private String LABTXT;
        private String PRDHA;
        private String ZEINR;
        private String BRGEW;
        private String GEWEI;
        private String NTGEW;
        private String VOLUM;
        private String GROES;
        private String EAN11;
        private String BREIT;
        private String HOEHE;
        private String LAENG;
        private String MEABM;
        private String MHDRZ;
        private String MHDHB;
        private String IPRKZ;
        private String TAXKM;
        private String VTEXT;
        private String MVGR1;
        private String MVGR2;
        private String MVGR4;
        private String STPRS;
        private String PEINH;
        private String GLTS;
        private String YLTS;
        private String RLTS;
        private String ZDWHSZ;
        private String BKLAS;
        private String VBAMG;

        public String getMTART() {
            return MTART;
        }
        @XmlElement(name = "MTART")
        public void setMTART(String MTART) {
            this.MTART = MTART;
        }

        public String getMTBEZ() {
            return MTBEZ;
        }
        @XmlElement(name = "MTBEZ")
        public void setMTBEZ(String MTBEZ) {
            this.MTBEZ = MTBEZ;
        }

        public String getMATNR() {
            return MATNR;
        }
        @XmlElement(name = "MATNR")
        public void setMATNR(String MATNR) {
            this.MATNR = MATNR;
        }

        public String getMAKTX() {
            return MAKTX;
        }
        @XmlElement(name = "MAKTX")
        public void setMAKTX(String MAKTX) {
            this.MAKTX = MAKTX;
        }

        public String getNORMT() {
            return NORMT;
        }
        @XmlElement(name = "NORMT")
        public void setNORMT(String NORMT) {
            this.NORMT = NORMT;
        }

        public String getLVORM() {
            return LVORM;
        }
        @XmlElement(name = "LVORM")
        public void setLVORM(String LVORM) {
            this.LVORM = LVORM;
        }

        public String getMEINS() {
            return MEINS;
        }
        @XmlElement(name = "MEINS")
        public void setMEINS(String MEINS) {
            this.MEINS = MEINS;
        }

        public String getMSEHT() {
            return MSEHT;
        }
        @XmlElement(name = "MSEHT")
        public void setMSEHT(String MSEHT) {
            this.MSEHT = MSEHT;
        }

        public String getMEINS1() {
            return MEINS1;
        }
        @XmlElement(name = "MEINS1")
        public void setMEINS1(String MEINS1) {
            this.MEINS1 = MEINS1;
        }

        public String getMSEHT1() {
            return MSEHT1;
        }
        @XmlElement(name = "MSEHT1")
        public void setMSEHT1(String MSEHT1) {
            this.MSEHT1 = MSEHT1;
        }

        public String getRATE1() {
            return RATE1;
        }
        @XmlElement(name = "RATE1")
        public void setRATE1(String RATE1) {
            this.RATE1 = RATE1;
        }

        public String getMEINS2() {
            return MEINS2;
        }
        @XmlElement(name = "MEINS2")
        public void setMEINS2(String MEINS2) {
            this.MEINS2 = MEINS2;
        }

        public String getMSEHT2() {
            return MSEHT2;
        }
        @XmlElement(name = "MSEHT2")
        public void setMSEHT2(String MSEHT2) {
            this.MSEHT2 = MSEHT2;
        }

        public String getRATE2() {
            return RATE2;
        }
        @XmlElement(name = "RATE2")
        public void setRATE2(String RATE2) {
            this.RATE2 = RATE2;
        }

        public String getMEINS3() {
            return MEINS3;
        }
        @XmlElement(name = "MEINS3")
        public void setMEINS3(String MEINS3) {
            this.MEINS3 = MEINS3;
        }

        public String getMSEHT3() {
            return MSEHT3;
        }
        @XmlElement(name = "MSEHT3")
        public void setMSEHT3(String MSEHT3) {
            this.MSEHT3 = MSEHT3;
        }

        public String getRATE3() {
            return RATE3;
        }
        @XmlElement(name = "RATE3")
        public void setRATE3(String RATE3) {
            this.RATE3 = RATE3;
        }

        public String getMEINS4() {
            return MEINS4;
        }
        @XmlElement(name = "MEINS4")
        public void setMEINS4(String MEINS4) {
            this.MEINS4 = MEINS4;
        }

        public String getMSEHT4() {
            return MSEHT4;
        }
        @XmlElement(name = "MSEHT4")
        public void setMSEHT4(String MSEHT4) {
            this.MSEHT4 = MSEHT4;
        }

        public String getRATE4() {
            return RATE4;
        }
        @XmlElement(name = "RATE4")
        public void setRATE4(String RATE4) {
            this.RATE4 = RATE4;
        }

        public String getMEINS5() {
            return MEINS5;
        }
        @XmlElement(name = "MEINS5")
        public void setMEINS5(String MEINS5) {
            this.MEINS5 = MEINS5;
        }

        public String getMSEHT5() {
            return MSEHT5;
        }
        @XmlElement(name = "MSEHT5")
        public void setMSEHT5(String MSEHT5) {
            this.MSEHT5 = MSEHT5;
        }

        public String getRATE5() {
            return RATE5;
        }
        @XmlElement(name = "RATE5")
        public void setRATE5(String RATE5) {
            this.RATE5 = RATE5;
        }

        public String getMATKL() {
            return MATKL;
        }
        @XmlElement(name = "MATKL")
        public void setMATKL(String MATKL) {
            this.MATKL = MATKL;
        }

        public String getWGBEZ() {
            return WGBEZ;
        }
        @XmlElement(name = "WGBEZ")
        public void setWGBEZ(String WGBEZ) {
            this.WGBEZ = WGBEZ;
        }

        public String getMSTAE() {
            return MSTAE;
        }
        @XmlElement(name = "MSTAE")
        public void setMSTAE(String MSTAE) {
            this.MSTAE = MSTAE;
        }

        public String getLABOR() {
            return LABOR;
        }
        @XmlElement(name = "LABOR")
        public void setLABOR(String LABOR) {
            this.LABOR = LABOR;
        }

        public String getLABTXT() {
            return LABTXT;
        }
        @XmlElement(name = "LABTXT")
        public void setLABTXT(String LABTXT) {
            this.LABTXT = LABTXT;
        }

        public String getPRDHA() {
            return PRDHA;
        }
        @XmlElement(name = "PRDHA")
        public void setPRDHA(String PRDHA) {
            this.PRDHA = PRDHA;
        }

        public String getZEINR() {
            return ZEINR;
        }
        @XmlElement(name = "ZEINR")
        public void setZEINR(String ZEINR) {
            this.ZEINR = ZEINR;
        }

        public String getBRGEW() {
            return BRGEW;
        }
        @XmlElement(name = "BRGEW")
        public void setBRGEW(String BRGEW) {
            this.BRGEW = BRGEW;
        }

        public String getGEWEI() {
            return GEWEI;
        }
        @XmlElement(name = "GEWEI")
        public void setGEWEI(String GEWEI) {
            this.GEWEI = GEWEI;
        }

        public String getNTGEW() {
            return NTGEW;
        }
        @XmlElement(name = "NTGEW")
        public void setNTGEW(String NTGEW) {
            this.NTGEW = NTGEW;
        }

        public String getVOLUM() {
            return VOLUM;
        }
        @XmlElement(name = "VOLUM")
        public void setVOLUM(String VOLUM) {
            this.VOLUM = VOLUM;
        }

        public String getGROES() {
            return GROES;
        }
        @XmlElement(name = "GROES")
        public void setGROES(String GROES) {
            this.GROES = GROES;
        }

        public String getEAN11() {
            return EAN11;
        }
        @XmlElement(name = "EAN11")
        public void setEAN11(String EAN11) {
            this.EAN11 = EAN11;
        }

        public String getBREIT() {
            return BREIT;
        }
        @XmlElement(name = "BREIT")
        public void setBREIT(String BREIT) {
            this.BREIT = BREIT;
        }

        public String getHOEHE() {
            return HOEHE;
        }
        @XmlElement(name = "HOEHE")
        public void setHOEHE(String HOEHE) {
            this.HOEHE = HOEHE;
        }

        public String getLAENG() {
            return LAENG;
        }
        @XmlElement(name = "LAENG")
        public void setLAENG(String LAENG) {
            this.LAENG = LAENG;
        }

        public String getMEABM() {
            return MEABM;
        }
        @XmlElement(name = "MEABM")
        public void setMEABM(String MEABM) {
            this.MEABM = MEABM;
        }

        public String getMHDRZ() {
            return MHDRZ;
        }
        @XmlElement(name = "MHDRZ")
        public void setMHDRZ(String MHDRZ) {
            this.MHDRZ = MHDRZ;
        }

        public String getMHDHB() {
            return MHDHB;
        }
        @XmlElement(name = "MHDHB")
        public void setMHDHB(String MHDHB) {
            this.MHDHB = MHDHB;
        }

        public String getIPRKZ() {
            return IPRKZ;
        }
        @XmlElement(name = "IPRKZ")
        public void setIPRKZ(String IPRKZ) {
            this.IPRKZ = IPRKZ;
        }

        public String getTAXKM() {
            return TAXKM;
        }
        @XmlElement(name = "TAXKM")
        public void setTAXKM(String TAXKM) {
            this.TAXKM = TAXKM;
        }

        public String getVTEXT() {
            return VTEXT;
        }
        @XmlElement(name = "VTEXT")
        public void setVTEXT(String VTEXT) {
            this.VTEXT = VTEXT;
        }

        public String getMVGR1() {
            return MVGR1;
        }
        @XmlElement(name = "MVGR1")
        public void setMVGR1(String MVGR1) {
            this.MVGR1 = MVGR1;
        }

        public String getMVGR2() {
            return MVGR2;
        }
        @XmlElement(name = "MVGR2")
        public void setMVGR2(String MVGR2) {
            this.MVGR2 = MVGR2;
        }

        public String getMVGR4() {
            return MVGR4;
        }
        @XmlElement(name = "MVGR4")
        public void setMVGR4(String MVGR4) {
            this.MVGR4 = MVGR4;
        }

        public String getSTPRS() {
            return STPRS;
        }
        @XmlElement(name = "STPRS")
        public void setSTPRS(String STPRS) {
            this.STPRS = STPRS;
        }

        public String getPEINH() {
            return PEINH;
        }
        @XmlElement(name = "PEINH")
        public void setPEINH(String PEINH) {
            this.PEINH = PEINH;
        }

        public String getGLTS() {
            return GLTS;
        }
        @XmlElement(name = "GLTS")
        public void setGLTS(String GLTS) {
            this.GLTS = GLTS;
        }

        public String getYLTS() {
            return YLTS;
        }
        @XmlElement(name = "YLTS")
        public void setYLTS(String YLTS) {
            this.YLTS = YLTS;
        }

        public String getRLTS() {
            return RLTS;
        }
        @XmlElement(name = "RLTS")
        public void setRLTS(String RLTS) {
            this.RLTS = RLTS;
        }

        public String getZDWHSZ() {
            return ZDWHSZ;
        }
        @XmlElement(name = "ZDWHSZ")
        public void setZDWHSZ(String ZDWHSZ) {
            this.ZDWHSZ = ZDWHSZ;
        }

        public String getBKLAS() {
            return BKLAS;
        }
        @XmlElement(name = "BKLAS")
        public void setBKLAS(String BKLAS) {
            this.BKLAS = BKLAS;
        }

        public String getVBAMG() {
            return VBAMG;
        }
        @XmlElement(name = "VBAMG")
        public void setVBAMG(String VBAMG) {
            this.VBAMG = VBAMG;
        }
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

}