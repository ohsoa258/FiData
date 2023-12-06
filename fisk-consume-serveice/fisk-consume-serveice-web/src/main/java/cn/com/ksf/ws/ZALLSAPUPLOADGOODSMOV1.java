
package cn.com.ksf.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ZALLSAP_UPLOAD_GOODSMOV_1 complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="ZALLSAP_UPLOAD_GOODSMOV_1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="BSART" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="EBELN" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="I_DATE" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="I_TIME" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="LGPLA" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="VTXTK" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="HTEXT" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="BUDAT" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ZALLSAP_UPLOAD_GOODSMOV_1", propOrder = {
    "bsart",
    "ebeln",
    "idate",
    "itime",
    "lgpla",
    "vtxtk",
    "htext",
    "budat"
})
public class ZALLSAPUPLOADGOODSMOV1 {

    @XmlElement(name = "BSART", required = true, nillable = true)
    protected String bsart;
    @XmlElement(name = "EBELN", required = true, nillable = true)
    protected String ebeln;
    @XmlElement(name = "I_DATE", required = true, nillable = true)
    protected String idate;
    @XmlElement(name = "I_TIME", required = true, nillable = true)
    protected String itime;
    @XmlElement(name = "LGPLA", required = true, nillable = true)
    protected String lgpla;
    @XmlElement(name = "VTXTK", required = true, nillable = true)
    protected String vtxtk;
    @XmlElement(name = "HTEXT", required = true, nillable = true)
    protected String htext;
    @XmlElement(name = "BUDAT", required = true, nillable = true)
    protected String budat;

    /**
     * 获取bsart属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBSART() {
        return bsart;
    }

    /**
     * 设置bsart属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBSART(String value) {
        this.bsart = value;
    }

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
     * 获取idate属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIDATE() {
        return idate;
    }

    /**
     * 设置idate属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIDATE(String value) {
        this.idate = value;
    }

    /**
     * 获取itime属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getITIME() {
        return itime;
    }

    /**
     * 设置itime属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setITIME(String value) {
        this.itime = value;
    }

    /**
     * 获取lgpla属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLGPLA() {
        return lgpla;
    }

    /**
     * 设置lgpla属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLGPLA(String value) {
        this.lgpla = value;
    }

    /**
     * 获取vtxtk属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVTXTK() {
        return vtxtk;
    }

    /**
     * 设置vtxtk属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVTXTK(String value) {
        this.vtxtk = value;
    }

    /**
     * 获取htext属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHTEXT() {
        return htext;
    }

    /**
     * 设置htext属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHTEXT(String value) {
        this.htext = value;
    }

    /**
     * 获取budat属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBUDAT() {
        return budat;
    }

    /**
     * 设置budat属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBUDAT(String value) {
        this.budat = value;
    }

}
