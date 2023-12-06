
package com.example.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ArrayOfZALLSAP_UPLOAD_GOODSMOV_1 complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="ArrayOfZALLSAP_UPLOAD_GOODSMOV_1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ZALLSAP_UPLOAD_GOODSMOV_1" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ZALLSAP_UPLOAD_GOODSMOV_1" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfZALLSAP_UPLOAD_GOODSMOV_1", propOrder = {
    "zallsapuploadgoodsmov1"
})
public class ArrayOfZALLSAPUPLOADGOODSMOV1 {

    @XmlElement(name = "ZALLSAP_UPLOAD_GOODSMOV_1")
    protected List<ZALLSAPUPLOADGOODSMOV1> zallsapuploadgoodsmov1;

    /**
     * Gets the value of the zallsapuploadgoodsmov1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the zallsapuploadgoodsmov1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getZALLSAPUPLOADGOODSMOV1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ZALLSAPUPLOADGOODSMOV1 }
     * 
     * 
     */
    public List<ZALLSAPUPLOADGOODSMOV1> getZALLSAPUPLOADGOODSMOV1() {
        if (zallsapuploadgoodsmov1 == null) {
            zallsapuploadgoodsmov1 = new ArrayList<ZALLSAPUPLOADGOODSMOV1>();
        }
        return this.zallsapuploadgoodsmov1;
    }

}
