
package com.example.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SOURCESYS" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="TARGETSYS" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="UPDATETIME" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="IT_MATDOC_DETAILS" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZALLSAP_UPLOAD_GOODSMOV_2"/&gt;
 *         &lt;element name="IT_MATDOC_HEAD" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZALLSAP_UPLOAD_GOODSMOV_1"/&gt;
 *         &lt;element name="OT_MATDOC" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZALLSAP_UPLOAD_GOODSMOV_3"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sourcesys",
    "targetsys",
    "updatetime",
    "itmatdocdetails",
    "itmatdochead",
    "otmatdoc"
})
@XmlRootElement(name = "ZALLSAP_UPLOAD_GOODSMOV", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/")
public class ZALLSAPUPLOADGOODSMOV {

    @XmlElement(name = "SOURCESYS", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", required = true, nillable = true)
    protected String sourcesys;
    @XmlElement(name = "TARGETSYS", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", required = true, nillable = true)
    protected String targetsys;
    @XmlElement(name = "UPDATETIME", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", required = true, nillable = true)
    protected String updatetime;
    @XmlElement(name = "IT_MATDOC_DETAILS", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", required = true, nillable = true)
    protected ArrayOfZALLSAPUPLOADGOODSMOV2 itmatdocdetails;
    @XmlElement(name = "IT_MATDOC_HEAD", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", required = true, nillable = true)
    protected ArrayOfZALLSAPUPLOADGOODSMOV1 itmatdochead;
    @XmlElement(name = "OT_MATDOC", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", required = true, nillable = true)
    protected ArrayOfZALLSAPUPLOADGOODSMOV3 otmatdoc;

    /**
     * 获取sourcesys属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSOURCESYS() {
        return sourcesys;
    }

    /**
     * 设置sourcesys属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSOURCESYS(String value) {
        this.sourcesys = value;
    }

    /**
     * 获取targetsys属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTARGETSYS() {
        return targetsys;
    }

    /**
     * 设置targetsys属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTARGETSYS(String value) {
        this.targetsys = value;
    }

    /**
     * 获取updatetime属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUPDATETIME() {
        return updatetime;
    }

    /**
     * 设置updatetime属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUPDATETIME(String value) {
        this.updatetime = value;
    }

    /**
     * 获取itmatdocdetails属性的值。
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfZALLSAPUPLOADGOODSMOV2 }
     *     
     */
    public ArrayOfZALLSAPUPLOADGOODSMOV2 getITMATDOCDETAILS() {
        return itmatdocdetails;
    }

    /**
     * 设置itmatdocdetails属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfZALLSAPUPLOADGOODSMOV2 }
     *     
     */
    public void setITMATDOCDETAILS(ArrayOfZALLSAPUPLOADGOODSMOV2 value) {
        this.itmatdocdetails = value;
    }

    /**
     * 获取itmatdochead属性的值。
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfZALLSAPUPLOADGOODSMOV1 }
     *     
     */
    public ArrayOfZALLSAPUPLOADGOODSMOV1 getITMATDOCHEAD() {
        return itmatdochead;
    }

    /**
     * 设置itmatdochead属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfZALLSAPUPLOADGOODSMOV1 }
     *     
     */
    public void setITMATDOCHEAD(ArrayOfZALLSAPUPLOADGOODSMOV1 value) {
        this.itmatdochead = value;
    }

    /**
     * 获取otmatdoc属性的值。
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfZALLSAPUPLOADGOODSMOV3 }
     *     
     */
    public ArrayOfZALLSAPUPLOADGOODSMOV3 getOTMATDOC() {
        return otmatdoc;
    }

    /**
     * 设置otmatdoc属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfZALLSAPUPLOADGOODSMOV3 }
     *     
     */
    public void setOTMATDOC(ArrayOfZALLSAPUPLOADGOODSMOV3 value) {
        this.otmatdoc = value;
    }

}
