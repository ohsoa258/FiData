
package cn.com.ksf.ws;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ZALLSAP_UPLOAD_GOODSMOV_2 complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="ZALLSAP_UPLOAD_GOODSMOV_2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EBELN" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="POSNR" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="SGTXT" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="MATNR" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="MENGE" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/&gt;
 *         &lt;element name="MEINS" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="WERKS" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="LGORT" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="INSMK" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="DZUSCH" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="HSDAT" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="LICHA" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="CHARG" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ELIKZ" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ZALLSAP_UPLOAD_GOODSMOV_2", propOrder = {
    "ebeln",
    "posnr",
    "sgtxt",
    "matnr",
    "menge",
    "meins",
    "werks",
    "lgort",
    "insmk",
    "dzusch",
    "hsdat",
    "licha",
    "charg",
    "elikz"
})
public class ZALLSAPUPLOADGOODSMOV2 {

    @XmlElement(name = "EBELN", required = true, nillable = true)
    protected String ebeln;
    @XmlElement(name = "POSNR", required = true, nillable = true)
    protected String posnr;
    @XmlElement(name = "SGTXT", required = true, nillable = true)
    protected String sgtxt;
    @XmlElement(name = "MATNR", required = true, nillable = true)
    protected String matnr;
    @XmlElementRef(name = "MENGE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<BigDecimal> menge;
    @XmlElement(name = "MEINS", required = true, nillable = true)
    protected String meins;
    @XmlElement(name = "WERKS", required = true, nillable = true)
    protected String werks;
    @XmlElement(name = "LGORT", required = true, nillable = true)
    protected String lgort;
    @XmlElement(name = "INSMK", required = true, nillable = true)
    protected String insmk;
    @XmlElement(name = "INSMK", required = true, nillable = true)
    protected String dzusch;
    @XmlElement(name = "HSDAT", required = true, nillable = true)
    protected String hsdat;
    @XmlElement(name = "LICHA", required = true, nillable = true)
    protected String licha;
    @XmlElement(name = "CHARG", required = true, nillable = true)
    protected String charg;
    @XmlElement(name = "ELIKZ", required = true, nillable = true)
    protected String elikz;

    /**
     * 获取ebeln属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEBELN() {
        return ebeln;
    }

    /**
     * 设置ebeln属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEBELN(String value) {
        this.ebeln = value;
    }

    /**
     * 获取posnr属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPOSNR() {
        return posnr;
    }

    /**
     * 设置posnr属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPOSNR(String value) {
        this.posnr = value;
    }

    /**
     * 获取sgtxt属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSGTXT() {
        return sgtxt;
    }

    /**
     * 设置sgtxt属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSGTXT(String value) {
        this.sgtxt = value;
    }

    /**
     * 获取matnr属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMATNR() {
        return matnr;
    }

    /**
     * 设置matnr属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMATNR(String value) {
        this.matnr = value;
    }

    /**
     * 获取menge属性的值。
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *     
     */
    public JAXBElement<BigDecimal> getMENGE() {
        return menge;
    }

    /**
     * 设置menge属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *     
     */
    public void setMENGE(JAXBElement<BigDecimal> value) {
        this.menge = value;
    }

    /**
     * 获取meins属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMEINS() {
        return meins;
    }

    /**
     * 设置meins属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMEINS(String value) {
        this.meins = value;
    }

    /**
     * 获取werks属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWERKS() {
        return werks;
    }

    /**
     * 设置werks属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWERKS(String value) {
        this.werks = value;
    }

    /**
     * 获取lgort属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLGORT() {
        return lgort;
    }

    /**
     * 设置lgort属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLGORT(String value) {
        this.lgort = value;
    }

    /**
     * 获取insmk属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getINSMK() {
        return insmk;
    }

    /**
     * 设置insmk属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setINSMK(String value) {
        this.insmk = value;
    }


    public String getDZUSCH() {
        return dzusch;
    }

    public void setDZUSCH(String dzusch) {
        this.dzusch = dzusch;
    }

    /**
     * 获取hsdat属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHSDAT() {
        return hsdat;
    }

    /**
     * 设置hsdat属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHSDAT(String value) {
        this.hsdat = value;
    }

    /**
     * 获取licha属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLICHA() {
        return licha;
    }

    /**
     * 设置licha属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLICHA(String value) {
        this.licha = value;
    }

    /**
     * 获取charg属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCHARG() {
        return charg;
    }

    /**
     * 设置charg属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCHARG(String value) {
        this.charg = value;
    }

    /**
     * 获取elikz属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getELIKZ() {
        return elikz;
    }

    /**
     * 设置elikz属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setELIKZ(String value) {
        this.elikz = value;
    }

}
