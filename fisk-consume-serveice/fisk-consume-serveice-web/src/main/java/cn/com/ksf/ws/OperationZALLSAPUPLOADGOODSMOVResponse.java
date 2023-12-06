
package cn.com.ksf.ws;

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
 *         &lt;element ref="{http://Microsoft.LobServices.Sap/2007/03/Rfc/}ZALLSAP_UPLOAD_GOODSMOVResponse" minOccurs="0"/&gt;
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
    "zallsapuploadgoodsmovResponse"
})
@XmlRootElement(name = "Operation_ZALLSAP_UPLOAD_GOODSMOVResponse", namespace = "http://ws.ksf.com.cn/")
public class OperationZALLSAPUPLOADGOODSMOVResponse {

    @XmlElement(name = "ZALLSAP_UPLOAD_GOODSMOVResponse", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/")
    protected ZALLSAPUPLOADGOODSMOVResponse zallsapuploadgoodsmovResponse;

    /**
     * 获取zallsapuploadgoodsmovResponse属性的值。
     * 
     * @return
     *     possible object is
     *     {@link ZALLSAPUPLOADGOODSMOVResponse }
     *     
     */
    public ZALLSAPUPLOADGOODSMOVResponse getZALLSAPUPLOADGOODSMOVResponse() {
        return zallsapuploadgoodsmovResponse;
    }

    /**
     * 设置zallsapuploadgoodsmovResponse属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link ZALLSAPUPLOADGOODSMOVResponse }
     *     
     */
    public void setZALLSAPUPLOADGOODSMOVResponse(ZALLSAPUPLOADGOODSMOVResponse value) {
        this.zallsapuploadgoodsmovResponse = value;
    }

}
