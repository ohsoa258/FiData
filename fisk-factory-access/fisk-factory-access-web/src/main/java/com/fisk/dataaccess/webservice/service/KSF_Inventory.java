package com.fisk.dataaccess.webservice.service;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;

/**
 * 前置机定制接口
 * 库存状态变更-业务数据行
 */
@Data
public class KSF_Inventory {

    /**
     * 物料凭证编号
     */
    @ApiModelProperty(value = "物料凭证编号")
    @JSONField(name = "MBLNR")
    private String MBLNR;

    /**
     * 物料凭证年度
     */
    @ApiModelProperty(value = "物料凭证年度")
    @JSONField(name = "MJAHR")
    private String MJAHR;

    /**
     * 凭证类型
     */
    @ApiModelProperty(value = "凭证类型")
    @JSONField(name = "BLART")
    private String BLART;

    /**
     * 凭证中的凭证日期
     */
    @ApiModelProperty(value = "凭证中的凭证日期")
    @JSONField(name = "BLDAT")
    private String BLDAT;

    /**
     * 凭证中的过账日期
     */
    @ApiModelProperty(value = "凭证中的过账日期")
    @JSONField(name = "BUDAT")
    private String BUDAT;

    /**
     * 会计凭证录入日期
     */
    @ApiModelProperty(value = "会计凭证录入日期")
    @JSONField(name = "CPUDT")
    private String CPUDT;

    /**
     * 输入时间
     */
    @ApiModelProperty(value = "输入时间")
    @JSONField(name = "CPUTM")
    private String CPUTM;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    @JSONField(name = "USNAM")
    private String USNAM;

    /**
     * 凭证抬头文本
     */
    @ApiModelProperty(value = "凭证抬头文本")
    @JSONField(name = "BKTXT")
    private String BKTXT;

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
     * 物料类型
     */
    @ApiModelProperty(value = "物料类型")
    @JSONField(name = "MTART")
    private String MTART;

    /**
     * 物料组
     */
    @ApiModelProperty(value = "物料组")
    @JSONField(name = "MATKL")
    private String MATKL;

    /**
     * 项目自动创建
     */
    @ApiModelProperty(value = "项目自动创建")
    @JSONField(name = "XAUTO")
    private String XAUTO;

    /**
     * 移动标识
     */
    @ApiModelProperty(value = "移动标识")
    @JSONField(name = "KZBEW")
    private String KZBEW;

    /**
     * 收货标识
     */
    @ApiModelProperty(value = "收货标识")
    @JSONField(name = "KZZUG")
    private String KZZUG;

    /**
     * 物料凭证中的项目
     */
    @ApiModelProperty(value = "物料凭证中的项目")
    @JSONField(name = "ZEILE")
    private String ZEILE;

    /**
     * 移动类型
     */
    @ApiModelProperty(value = "移动类型")
    @JSONField(name = "BWART")
    private String BWART;

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
     * 工厂
     */
    @ApiModelProperty(value = "工厂")
    @JSONField(name = "WERKS")
    private String WERKS;

    /**
     * 工厂名称
     */
    @ApiModelProperty(value = "工厂名称")
    @JSONField(name = "WNAME1")
    private String WNAME1;

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
     * 批号
     */
    @ApiModelProperty(value = "ApiModelProperty")
    @JSONField(name = "CHARG")
    private String CHARG;

    /**
     * 库存类型
     */
    @ApiModelProperty(value = "库存类型")
    @JSONField(name = "INSMK")
    private String INSMK;

    /**
     * 特殊库存标识
     */
    @ApiModelProperty(value = "特殊库存标识")
    @JSONField(name = "SOBKZ")
    private String SOBKZ;

    /**
     * 数量
     */
    @ApiModelProperty(value = "数量")
    @JSONField(name = "MENGE")
    private String MENGE;

    /**
     * 基本计量单位
     */
    @ApiModelProperty(value = "基本计量单位")
    @JSONField(name = "MEINS")
    private String MEINS;

    /**
     * 以输入单位计的数量
     */
    @ApiModelProperty(value = "以输入单位计的数量")
    @JSONField(name = "ERFMG")
    private String ERFMG;

    /**
     * 条目单位
     */
    @ApiModelProperty(value = "条目单位")
    @JSONField(name = "ERFME")
    private String ERFME;

    /**
     * 按本位币计的金额
     */
    @ApiModelProperty(value = "按本位币计的金额")
    @JSONField(name = "DMBTR")
    private String DMBTR;

    /**
     * 货币码
     */
    @ApiModelProperty(value = "货币码")
    @JSONField(name = "WAERS")
    private String WAERS;

    /**
     * 供应商帐户号
     */
    @ApiModelProperty(value = "供应商帐户号")
    @JSONField(name = "LIFNR")
    private String LIFNR;

    /**
     * 供应商名称
     */
    @ApiModelProperty(value = "供应商名称")
    @JSONField(name = "LNAME1")
    private String LNAME1;

    /**
     * 采购订单编号
     */
    @ApiModelProperty(value = "采购订单编号")
    @JSONField(name = "EBELN")
    private String EBELN;

    /**
     * 采购凭证的项目编号
     */
    @ApiModelProperty(value = "采购凭证的项目编号")
    @JSONField(name = "EBELP")
    private String EBELP;

    /**
     * 客户的帐户号
     */
    @ApiModelProperty(value = "客户的帐户号")
    @JSONField(name = "KUNNR")
    private String KUNNR;

    /**
     * 客户名称
     */
    @ApiModelProperty(value = "客户名称")
    @JSONField(name = "KNAME1")
    private String KNAME1;

    /**
     * 收货方/运达方
     */
    @ApiModelProperty(value = "收货方/运达方")
    @JSONField(name = "WEMPF")
    private String WEMPF;

    /**
     * 销售订单数
     */
    @ApiModelProperty(value = "销售订单数")
    @JSONField(name = "KDAUF")
    private String KDAUF;

    /**
     * 销售订单中的项目编号
     */
    @ApiModelProperty(value = "销售订单中的项目编号")
    @JSONField(name = "KDPOS")
    private String KDPOS;

    /**
     * 参考凭证号
     */
    @ApiModelProperty(value = "参考凭证号")
    @JSONField(name = "XBLNR")
    private String XBLNR;

    /**
     * 公司代码
     */
    @ApiModelProperty(value = "公司代码")
    @JSONField(name = "BUKRS")
    private String BUKRS;

    /**
     * 公司代码或公司的名称
     */
    @ApiModelProperty(value = "公司代码或公司的名称")
    @JSONField(name = "BUTXT")
    private String BUTXT;

    /**
     * 会计凭证编号
     */
    @ApiModelProperty(value = "会计凭证编号")
    @JSONField(name = "BELNR")
    private String BELNR;

    /**
     * 总帐科目编号
     */
    @ApiModelProperty(value = "总帐科目编号")
    @JSONField(name = "SAKTO")
    private String SAKTO;

    /**
     * 成本中心
     */
    @ApiModelProperty(value = "成本中心")
    @JSONField(name = "KOSTL")
    private String KOSTL;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "ApiModelProperty")
    @JSONField(name = "AUFNR")
    private String AUFNR;

    /**
     * 主资产号
     */
    @ApiModelProperty(value = "主资产号")
    @JSONField(name = "ANLN1")
    private String ANLN1;

    /**
     * 货架寿命到期日
     */
    @ApiModelProperty(value = "货架寿命到期日")
    @JSONField(name = "VFDAT")
    private String VFDAT;

    /**
     * 生产日期
     */
    @ApiModelProperty(value = "生产日期")
    @JSONField(name = "HSDAT")
    private String HSDAT;

    /**
     * 收货/发货物料
     */
    @ApiModelProperty(value = "收货/发货物料")
    @JSONField(name = "UMMAT")
    private String UMMAT;

    /**
     * 收货/发货工厂
     */
    @ApiModelProperty(value = "收货/发货工厂")
    @JSONField(name = "UMWRK")
    private String UMWRK;

    /**
     * 收货/发货库存地点
     */
    @ApiModelProperty(value = "收货/发货库存地点")
    @JSONField(name = "UMLGO")
    private String UMLGO;

    /**
     * 收货/发货批量
     */
    @ApiModelProperty(value = "收货/发货批量")
    @JSONField(name = "UMCHA")
    private String UMCHA;

    /**
     * 移动原因
     */
    @ApiModelProperty(value = "移动原因")
    @JSONField(name = "GRUND")
    private String GRUND;

    /**
     * 项目文本（申请用途）
     */
    @ApiModelProperty(value = "项目文本（申请用途）")
    @JSONField(name = "SGTXT")
    private String SGTXT;

    @XmlElement(name = "MBLNR", nillable = false, required = true)
    public String getMBLNR() {
        return MBLNR;
    }

    public void setMBLNR(String MBLNR) {
        this.MBLNR = MBLNR;
    }

    public String getMJAHR() {
        return MJAHR;
    }

    public void setMJAHR(String MJAHR) {
        this.MJAHR = MJAHR;
    }

    public String getBLART() {
        return BLART;
    }

    public void setBLART(String BLART) {
        this.BLART = BLART;
    }

    public String getBLDAT() {
        return BLDAT;
    }

    public void setBLDAT(String BLDAT) {
        this.BLDAT = BLDAT;
    }

    @XmlElement(name = "BUDAT", nillable = false, required = true)
    public String getBUDAT() {
        return BUDAT;
    }

    public void setBUDAT(String BUDAT) {
        this.BUDAT = BUDAT;
    }

    public String getCPUDT() {
        return CPUDT;
    }

    public void setCPUDT(String CPUDT) {
        this.CPUDT = CPUDT;
    }

    public String getCPUTM() {
        return CPUTM;
    }

    public void setCPUTM(String CPUTM) {
        this.CPUTM = CPUTM;
    }

    public String getUSNAM() {
        return USNAM;
    }

    public void setUSNAM(String USNAM) {
        this.USNAM = USNAM;
    }

    @XmlElement(name = "BKTXT", nillable = false, required = true)
    public String getBKTXT() {
        return BKTXT;
    }

    public void setBKTXT(String BKTXT) {
        this.BKTXT = BKTXT;
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

    public String getMTART() {
        return MTART;
    }

    public void setMTART(String MTART) {
        this.MTART = MTART;
    }

    public String getMATKL() {
        return MATKL;
    }

    public void setMATKL(String MATKL) {
        this.MATKL = MATKL;
    }

    public String getXAUTO() {
        return XAUTO;
    }

    public void setXAUTO(String XAUTO) {
        this.XAUTO = XAUTO;
    }

    public String getKZBEW() {
        return KZBEW;
    }

    public void setKZBEW(String KZBEW) {
        this.KZBEW = KZBEW;
    }

    public String getKZZUG() {
        return KZZUG;
    }

    public void setKZZUG(String KZZUG) {
        this.KZZUG = KZZUG;
    }

    @XmlElement(name = "ZEILE", nillable = false, required = true)
    public String getZEILE() {
        return ZEILE;
    }

    public void setZEILE(String ZEILE) {
        this.ZEILE = ZEILE;
    }

    @XmlElement(name = "BWART", nillable = false, required = true)
    public String getBWART() {
        return BWART;
    }

    public void setBWART(String BWART) {
        this.BWART = BWART;
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

    @XmlElement(name = "WERKS", nillable = false, required = true)
    public String getWERKS() {
        return WERKS;
    }

    public void setWERKS(String WERKS) {
        this.WERKS = WERKS;
    }

    @XmlElement(name = "WNAME1", nillable = false, required = true)
    public String getWNAME1() {
        return WNAME1;
    }

    public void setWNAME1(String WNAME1) {
        this.WNAME1 = WNAME1;
    }

    @XmlElement(name = "LGORT", nillable = false, required = true)
    public String getLGORT() {
        return LGORT;
    }

    public void setLGORT(String LGORT) {
        this.LGORT = LGORT;
    }

    @XmlElement(name = "LGOBE", nillable = false, required = true)
    public String getLGOBE() {
        return LGOBE;
    }

    public void setLGOBE(String LGOBE) {
        this.LGOBE = LGOBE;
    }

    @XmlElement(name = "CHARG", nillable = false, required = true)
    public String getCHARG() {
        return CHARG;
    }

    public void setCHARG(String CHARG) {
        this.CHARG = CHARG;
    }

    @XmlElement(name = "INSMK", nillable = false, required = true)
    public String getINSMK() {
        return INSMK;
    }

    public void setINSMK(String INSMK) {
        this.INSMK = INSMK;
    }

    public String getSOBKZ() {
        return SOBKZ;
    }

    public void setSOBKZ(String SOBKZ) {
        this.SOBKZ = SOBKZ;
    }

    @XmlElement(name = "MENGE", nillable = false, required = true)
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

    public String getERFMG() {
        return ERFMG;
    }

    public void setERFMG(String ERFMG) {
        this.ERFMG = ERFMG;
    }

    @XmlElement(name = "ERFME", nillable = false, required = true)
    public String getERFME() {
        return ERFME;
    }

    public void setERFME(String ERFME) {
        this.ERFME = ERFME;
    }

    public String getDMBTR() {
        return DMBTR;
    }

    public void setDMBTR(String DMBTR) {
        this.DMBTR = DMBTR;
    }

    public String getWAERS() {
        return WAERS;
    }

    public void setWAERS(String WAERS) {
        this.WAERS = WAERS;
    }

    public String getLIFNR() {
        return LIFNR;
    }

    public void setLIFNR(String LIFNR) {
        this.LIFNR = LIFNR;
    }

    public String getLNAME1() {
        return LNAME1;
    }

    public void setLNAME1(String LNAME1) {
        this.LNAME1 = LNAME1;
    }

    public String getEBELN() {
        return EBELN;
    }

    public void setEBELN(String EBELN) {
        this.EBELN = EBELN;
    }

    public String getEBELP() {
        return EBELP;
    }

    public void setEBELP(String EBELP) {
        this.EBELP = EBELP;
    }

    public String getKUNNR() {
        return KUNNR;
    }

    public void setKUNNR(String KUNNR) {
        this.KUNNR = KUNNR;
    }

    public String getKNAME1() {
        return KNAME1;
    }

    public void setKNAME1(String KNAME1) {
        this.KNAME1 = KNAME1;
    }

    public String getWEMPF() {
        return WEMPF;
    }

    public void setWEMPF(String WEMPF) {
        this.WEMPF = WEMPF;
    }

    public String getKDAUF() {
        return KDAUF;
    }

    public void setKDAUF(String KDAUF) {
        this.KDAUF = KDAUF;
    }

    public String getKDPOS() {
        return KDPOS;
    }

    public void setKDPOS(String KDPOS) {
        this.KDPOS = KDPOS;
    }

    public String getXBLNR() {
        return XBLNR;
    }

    public void setXBLNR(String XBLNR) {
        this.XBLNR = XBLNR;
    }

    public String getBUKRS() {
        return BUKRS;
    }

    public void setBUKRS(String BUKRS) {
        this.BUKRS = BUKRS;
    }

    public String getBUTXT() {
        return BUTXT;
    }

    public void setBUTXT(String BUTXT) {
        this.BUTXT = BUTXT;
    }

    public String getBELNR() {
        return BELNR;
    }

    public void setBELNR(String BELNR) {
        this.BELNR = BELNR;
    }

    public String getSAKTO() {
        return SAKTO;
    }

    public void setSAKTO(String SAKTO) {
        this.SAKTO = SAKTO;
    }

    public String getKOSTL() {
        return KOSTL;
    }

    public void setKOSTL(String KOSTL) {
        this.KOSTL = KOSTL;
    }

    public String getAUFNR() {
        return AUFNR;
    }

    public void setAUFNR(String AUFNR) {
        this.AUFNR = AUFNR;
    }

    public String getANLN1() {
        return ANLN1;
    }

    public void setANLN1(String ANLN1) {
        this.ANLN1 = ANLN1;
    }

    public String getVFDAT() {
        return VFDAT;
    }

    public void setVFDAT(String VFDAT) {
        this.VFDAT = VFDAT;
    }

    public String getHSDAT() {
        return HSDAT;
    }

    public void setHSDAT(String HSDAT) {
        this.HSDAT = HSDAT;
    }

    public String getUMMAT() {
        return UMMAT;
    }

    public void setUMMAT(String UMMAT) {
        this.UMMAT = UMMAT;
    }

    public String getUMWRK() {
        return UMWRK;
    }

    public void setUMWRK(String UMWRK) {
        this.UMWRK = UMWRK;
    }

    public String getUMLGO() {
        return UMLGO;
    }

    public void setUMLGO(String UMLGO) {
        this.UMLGO = UMLGO;
    }

    public String getUMCHA() {
        return UMCHA;
    }

    public void setUMCHA(String UMCHA) {
        this.UMCHA = UMCHA;
    }

    public String getGRUND() {
        return GRUND;
    }

    public void setGRUND(String GRUND) {
        this.GRUND = GRUND;
    }

    @XmlElement(name = "SGTXT", nillable = false, required = true)
    public String getSGTXT() {
        return SGTXT;
    }

    public void setSGTXT(String SGTXT) {
        this.SGTXT = SGTXT;
    }
}
