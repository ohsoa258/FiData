package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;

/**
 * 前置机定制接口
 * 物料主数据-业务数据行
 */
@Data
public class ItemData {

    /**
     * 物料类型
     */
    @ApiModelProperty(value = "物料类型")
    @JSONField(name = "MTART")
    private String MTART;

    /**
     * 物料类型描述
     */
    @ApiModelProperty(value = "物料类型描述")
    @JSONField(name = "MTBEZ")
    private String MTBEZ;

    /**
     * 物料号
     */
    @ApiModelProperty(value = "物料号")
    @JSONField(name = "MATNR")
    private String MATNR;

    /**
     * 物料描述
     */
    @ApiModelProperty(value = "物料描述")
    @JSONField(name = "MAKTX")
    private String MAKTX;

    /**
     * 物料简称
     */
    @ApiModelProperty(value = "物料简称")
    @JSONField(name = "NORMT")
    private String NORMT;

    /**
     * 删除标识
     */
    @ApiModelProperty(value = "删除标识")
    @JSONField(name = "LVORM")
    private String LVORM;

    /**
     * 基本计量单位
     */
    @ApiModelProperty(value = "基本计量单位")
    @JSONField(name = "MEINS")
    private String MEINS;

    /**
     * 基本计量单位描述
     */
    @ApiModelProperty(value = "基本计量单位描述")
    @JSONField(name = "MSEHT")
    private String MSEHT;

    /**
     * 附加计量单位1
     */
    @ApiModelProperty(value = "附加计量单位1")
    @JSONField(name = "MEINS1")
    private String MEINS1;

    /**
     * 附加计量单位描述1
     */
    @ApiModelProperty(value = "附加计量单位描述1")
    @JSONField(name = "MSEHT1")
    private String MSEHT1;

    /**
     * 转换关系1
     */
    @ApiModelProperty(value = "转换关系1")
    @JSONField(name = "RATE1")
    private String RATE1;

    /**
     * 附加计量单位2
     */
    @ApiModelProperty(value = "附加计量单位2")
    @JSONField(name = "MEINS2")
    private String MEINS2;

    /**
     * 附加计量单位描述2
     */
    @ApiModelProperty(value = "附加计量单位描述2")
    @JSONField(name = "MSEHT2")
    private String MSEHT2;

    /**
     * 转换关系2
     */
    @ApiModelProperty(value = "转换关系2")
    @JSONField(name = "RATE2")
    private String RATE2;

    /**
     * 附加计量单位3
     */
    @ApiModelProperty(value = "附加计量单位3")
    @JSONField(name = "MEINS3")
    private String MEINS3;

    /**
     * 附加计量单位描述3
     */
    @ApiModelProperty(value = "附加计量单位描述3")
    @JSONField(name = "MSEHT3")
    private String MSEHT3;

    /**
     * 转换关系3
     */
    @ApiModelProperty(value = "转换关系3")
    @JSONField(name = "RATE3")
    private String RATE3;

    /**
     * 附加计量单位4
     */
    @ApiModelProperty(value = "附加计量单位4")
    @JSONField(name = "MEINS4")
    private String MEINS4;

    /**
     * 附加计量单位描述4
     */
    @ApiModelProperty(value = "附加计量单位描述4")
    @JSONField(name = "MSEHT4")
    private String MSEHT4;

    /**
     * 转换关系4
     */
    @ApiModelProperty(value = "转换关系4")
    @JSONField(name = "RATE4")
    private String RATE4;

    /**
     * 附加计量单位5
     */
    @ApiModelProperty(value = "附加计量单位5")
    @JSONField(name = "MEINS5")
    private String MEINS5;

    /**
     * 附加计量单位描述5
     */
    @ApiModelProperty(value = "抬头发货日期（ETD）")
    @JSONField(name = "BUDAT")
    private String MSEHT5;

    /**
     * 转换关系5
     */
    @ApiModelProperty(value = "转换关系5")
    @JSONField(name = "RATE5")
    private String RATE5;

    /**
     * 物料组
     */
    @ApiModelProperty(value = "物料组")
    @JSONField(name = "MATKL")
    private String MATKL;

    /**
     * 物料组描述
     */
    @ApiModelProperty(value = "物料组描述")
    @JSONField(name = "WGBEZ")
    private String WGBEZ;

    /**
     * 跨工厂物料状态
     */
    @ApiModelProperty(value = "跨工厂物料状态")
    @JSONField(name = "MSTAE")
    private String MSTAE;

    /**
     * 口味码
     */
    @ApiModelProperty(value = "口味码")
    @JSONField(name = "LABOR")
    private String LABOR;

    /**
     * 口味码的描述
     */
    @ApiModelProperty(value = "口味码的描述")
    @JSONField(name = "LABTXT")
    private String LABTXT;

    /**
     * 产品层次
     */
    @ApiModelProperty(value = "产品层次")
    @JSONField(name = "PRDHA")
    private String PRDHA;

    /**
     * 税收分类编码
     */
    @ApiModelProperty(value = "税收分类编码")
    @JSONField(name = "ZEINR")
    private String ZEINR;

    /**
     * 毛重/运费计算分摊系数
     */
    @ApiModelProperty(value = "毛重/运费计算分摊系数")
    @JSONField(name = "BRGEW")
    private String BRGEW;

    /**
     * 重量单位
     */
    @ApiModelProperty(value = "重量单位")
    @JSONField(name = "GEWEI")
    private String GEWEI;

    /**
     * 净重
     */
    @ApiModelProperty(value = "净重")
    @JSONField(name = "NTGEW")
    private String NTGEW;

    /**
     * 业务量
     */
    @ApiModelProperty(value = "业务量")
    @JSONField(name = "VOLUM")
    private String VOLUM;

    /**
     * 大小/量纲
     */
    @ApiModelProperty(value = "大小/量纲")
    @JSONField(name = "GROES")
    private String GROES;

    /**
     * 国际商品编码（欧洲商品编码/通用产品代码）
     */
    @ApiModelProperty(value = "国际商品编码（欧洲商品编码/通用产品代码）")
    @JSONField(name = "EAN11")
    private String EAN11;

    /**
     * 宽度
     */
    @ApiModelProperty(value = "宽度")
    @JSONField(name = "BREIT")
    private String BREIT;

    /**
     * 高度
     */
    @ApiModelProperty(value = "高度")
    @JSONField(name = "HOEHE")
    private String HOEHE;

    /**
     * 长度
     */
    @ApiModelProperty(value = "长度")
    @JSONField(name = "LAENG")
    private String LAENG;

    /**
     * 长度/宽度/高度的尺寸单位
     */
    @ApiModelProperty(value = "长度/宽度/高度的尺寸单位")
    @JSONField(name = "MEABM")
    private String MEABM;

    /**
     * 最短剩余货架寿命-工厂数据/存储1
     */
    @ApiModelProperty(value = "最短剩余货架寿命-工厂数据/存储1")
    @JSONField(name = "MHDRZ")
    private String MHDRZ;

    /**
     * 总货架寿命
     */
    @ApiModelProperty(value = "总货架寿命")
    @JSONField(name = "MHDHB")
    private String MHDHB;

    /**
     * 货架寿命到期日的期间标识
     */
    @ApiModelProperty(value = "货架寿命到期日的期间标识")
    @JSONField(name = "IPRKZ")
    private String IPRKZ;

    /**
     * 物料的税分类-销售：销售组织数据1
     */
    @ApiModelProperty(value = "物料的税分类-销售：销售组织数据1")
    @JSONField(name = "TAXKM")
    private String TAXKM;

    /**
     * 税分类描述
     */
    @ApiModelProperty(value = "税分类描述")
    @JSONField(name = "VTEXT")
    private String VTEXT;

    /**
     * 同价汇总码-销售：销售组织数据2
     */
    @ApiModelProperty(value = "同价汇总码-销售：销售组织数据2")
    @JSONField(name = "MVGR1")
    private String MVGR1;

    /**
     * 正常箱折算系数
     */
    @ApiModelProperty(value = "正常箱折算系数")
    @JSONField(name = "MVGR2")
    private String MVGR2;

    /**
     * 物料规格
     */
    @ApiModelProperty(value = "物料规格")
    @JSONField(name = "MVGR4")
    private String MVGR4;

    /**
     * 标准价格
     */
    @ApiModelProperty(value = "标准价格")
    @JSONField(name = "STPRS")
    private String STPRS;

    /**
     * 价格单位
     */
    @ApiModelProperty(value = "价格单位")
    @JSONField(name = "PEINH")
    private String PEINH;

    /**
     * 绿灯天数
     */
    @ApiModelProperty(value = "绿灯天数")
    @JSONField(name = "GLTS")
    private String GLTS;

    /**
     * 黄灯天数
     */
    @ApiModelProperty(value = "黄灯天数")
    @JSONField(name = "YLTS")
    private String YLTS;

    /**
     * 红灯天数
     */
    @ApiModelProperty(value = "红灯天数")
    @JSONField(name = "RLTS")
    private String RLTS;

    /**
     * 业务量
     */
    @ApiModelProperty(value = "业务量")
    @JSONField(name = "ZDWHSZ")
    private String ZDWHSZ;

    /**
     * 评估分类
     */
    @ApiModelProperty(value = "评估分类")
    @JSONField(name = "BKLAS")
    private String BKLAS;

    /**
     * 基准数量
     */
    @ApiModelProperty(value = "基准数量")
    @JSONField(name = "VBAMG")
    private String VBAMG;


    @XmlElement(name = "MTART", nillable = false, required = true)
    public String getMTART() {
        return MTART;
    }

    public void setMTART(String MTART) {
        this.MTART = MTART;
    }

    @XmlElement(name = "MTBEZ", nillable = false, required = true)
    public String getMTBEZ() {
        return MTBEZ;
    }

    public void setMTBEZ(String MTBEZ) {
        this.MTBEZ = MTBEZ;
    }

    @XmlElement(name = "MATNR", nillable = false, required = true)
    public String getMATNR() {
        return MATNR;
    }

    public void setMATNR(String MATNR) {
        this.MATNR = MATNR;
    }

    @XmlElement(name = "MAKTX", nillable = false, required = true)
    public String getMAKTX() {
        return MAKTX;
    }

    public void setMAKTX(String MAKTX) {
        this.MAKTX = MAKTX;
    }

    @XmlElement(name = "NORMT", nillable = false, required = true)
    public String getNORMT() {
        return NORMT;
    }

    public void setNORMT(String NORMT) {
        this.NORMT = NORMT;
    }

    public String getLVORM() {
        return LVORM;
    }

    public void setLVORM(String LVORM) {
        this.LVORM = LVORM;
    }

    @XmlElement(name = "MEINS", nillable = false, required = true)
    public String getMEINS() {
        return MEINS;
    }

    public void setMEINS(String MEINS) {
        this.MEINS = MEINS;
    }

    @XmlElement(name = "MSEHT", nillable = false, required = true)
    public String getMSEHT() {
        return MSEHT;
    }

    public void setMSEHT(String MSEHT) {
        this.MSEHT = MSEHT;
    }

    public String getMEINS1() {
        return MEINS1;
    }

    public void setMEINS1(String MEINS1) {
        this.MEINS1 = MEINS1;
    }

    public String getMSEHT1() {
        return MSEHT1;
    }

    public void setMSEHT1(String MSEHT1) {
        this.MSEHT1 = MSEHT1;
    }

    public String getRATE1() {
        return RATE1;
    }

    public void setRATE1(String RATE1) {
        this.RATE1 = RATE1;
    }

    public String getMEINS2() {
        return MEINS2;
    }

    public void setMEINS2(String MEINS2) {
        this.MEINS2 = MEINS2;
    }

    public String getMSEHT2() {
        return MSEHT2;
    }

    public void setMSEHT2(String MSEHT2) {
        this.MSEHT2 = MSEHT2;
    }

    public String getRATE2() {
        return RATE2;
    }

    public void setRATE2(String RATE2) {
        this.RATE2 = RATE2;
    }

    public String getMEINS3() {
        return MEINS3;
    }

    public void setMEINS3(String MEINS3) {
        this.MEINS3 = MEINS3;
    }

    public String getMSEHT3() {
        return MSEHT3;
    }

    public void setMSEHT3(String MSEHT3) {
        this.MSEHT3 = MSEHT3;
    }

    public String getRATE3() {
        return RATE3;
    }

    public void setRATE3(String RATE3) {
        this.RATE3 = RATE3;
    }

    public String getMEINS4() {
        return MEINS4;
    }

    public void setMEINS4(String MEINS4) {
        this.MEINS4 = MEINS4;
    }

    public String getMSEHT4() {
        return MSEHT4;
    }

    public void setMSEHT4(String MSEHT4) {
        this.MSEHT4 = MSEHT4;
    }

    public String getRATE4() {
        return RATE4;
    }

    public void setRATE4(String RATE4) {
        this.RATE4 = RATE4;
    }

    public String getMEINS5() {
        return MEINS5;
    }

    public void setMEINS5(String MEINS5) {
        this.MEINS5 = MEINS5;
    }

    public String getMSEHT5() {
        return MSEHT5;
    }

    public void setMSEHT5(String MSEHT5) {
        this.MSEHT5 = MSEHT5;
    }

    public String getRATE5() {
        return RATE5;
    }

    public void setRATE5(String RATE5) {
        this.RATE5 = RATE5;
    }

    @XmlElement(name = "MATKL", nillable = false, required = true)
    public String getMATKL() {
        return MATKL;
    }

    public void setMATKL(String MATKL) {
        this.MATKL = MATKL;
    }

    @XmlElement(name = "WGBEZ", nillable = false, required = true)
    public String getWGBEZ() {
        return WGBEZ;
    }

    public void setWGBEZ(String WGBEZ) {
        this.WGBEZ = WGBEZ;
    }

    public String getMSTAE() {
        return MSTAE;
    }

    public void setMSTAE(String MSTAE) {
        this.MSTAE = MSTAE;
    }

    public String getLABOR() {
        return LABOR;
    }

    public void setLABOR(String LABOR) {
        this.LABOR = LABOR;
    }

    public String getLABTXT() {
        return LABTXT;
    }

    public void setLABTXT(String LABTXT) {
        this.LABTXT = LABTXT;
    }

    @XmlElement(name = "PRDHA", nillable = false, required = true)
    public String getPRDHA() {
        return PRDHA;
    }

    public void setPRDHA(String PRDHA) {
        this.PRDHA = PRDHA;
    }

    public String getZEINR() {
        return ZEINR;
    }

    public void setZEINR(String ZEINR) {
        this.ZEINR = ZEINR;
    }

    public String getBRGEW() {
        return BRGEW;
    }

    public void setBRGEW(String BRGEW) {
        this.BRGEW = BRGEW;
    }

    public String getGEWEI() {
        return GEWEI;
    }

    public void setGEWEI(String GEWEI) {
        this.GEWEI = GEWEI;
    }

    public String getNTGEW() {
        return NTGEW;
    }

    public void setNTGEW(String NTGEW) {
        this.NTGEW = NTGEW;
    }

    public String getVOLUM() {
        return VOLUM;
    }

    public void setVOLUM(String VOLUM) {
        this.VOLUM = VOLUM;
    }

    public String getGROES() {
        return GROES;
    }

    public void setGROES(String GROES) {
        this.GROES = GROES;
    }

    public String getEAN11() {
        return EAN11;
    }

    public void setEAN11(String EAN11) {
        this.EAN11 = EAN11;
    }

    public String getBREIT() {
        return BREIT;
    }

    public void setBREIT(String BREIT) {
        this.BREIT = BREIT;
    }

    public String getHOEHE() {
        return HOEHE;
    }

    public void setHOEHE(String HOEHE) {
        this.HOEHE = HOEHE;
    }

    public String getLAENG() {
        return LAENG;
    }

    public void setLAENG(String LAENG) {
        this.LAENG = LAENG;
    }

    public String getMEABM() {
        return MEABM;
    }

    public void setMEABM(String MEABM) {
        this.MEABM = MEABM;
    }

    public String getMHDRZ() {
        return MHDRZ;
    }

    public void setMHDRZ(String MHDRZ) {
        this.MHDRZ = MHDRZ;
    }

    public String getMHDHB() {
        return MHDHB;
    }

    public void setMHDHB(String MHDHB) {
        this.MHDHB = MHDHB;
    }

    public String getIPRKZ() {
        return IPRKZ;
    }

    public void setIPRKZ(String IPRKZ) {
        this.IPRKZ = IPRKZ;
    }

    public String getTAXKM() {
        return TAXKM;
    }

    public void setTAXKM(String TAXKM) {
        this.TAXKM = TAXKM;
    }

    public String getVTEXT() {
        return VTEXT;
    }

    public void setVTEXT(String VTEXT) {
        this.VTEXT = VTEXT;
    }

    public String getMVGR1() {
        return MVGR1;
    }

    public void setMVGR1(String MVGR1) {
        this.MVGR1 = MVGR1;
    }

    public String getMVGR2() {
        return MVGR2;
    }

    public void setMVGR2(String MVGR2) {
        this.MVGR2 = MVGR2;
    }

    public String getMVGR4() {
        return MVGR4;
    }

    public void setMVGR4(String MVGR4) {
        this.MVGR4 = MVGR4;
    }

    public String getSTPRS() {
        return STPRS;
    }

    public void setSTPRS(String STPRS) {
        this.STPRS = STPRS;
    }

    public String getPEINH() {
        return PEINH;
    }

    public void setPEINH(String PEINH) {
        this.PEINH = PEINH;
    }

    public String getGLTS() {
        return GLTS;
    }

    public void setGLTS(String GLTS) {
        this.GLTS = GLTS;
    }

    public String getYLTS() {
        return YLTS;
    }

    public void setYLTS(String YLTS) {
        this.YLTS = YLTS;
    }

    public String getRLTS() {
        return RLTS;
    }

    public void setRLTS(String RLTS) {
        this.RLTS = RLTS;
    }

    public String getZDWHSZ() {
        return ZDWHSZ;
    }

    public void setZDWHSZ(String ZDWHSZ) {
        this.ZDWHSZ = ZDWHSZ;
    }

    public String getBKLAS() {
        return BKLAS;
    }

    public void setBKLAS(String BKLAS) {
        this.BKLAS = BKLAS;
    }

    public String getVBAMG() {
        return VBAMG;
    }

    public void setVBAMG(String VBAMG) {
        this.VBAMG = VBAMG;
    }
}
