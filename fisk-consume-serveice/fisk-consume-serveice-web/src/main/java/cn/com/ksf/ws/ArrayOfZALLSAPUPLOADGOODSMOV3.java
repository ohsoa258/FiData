
package cn.com.ksf.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ArrayOfZALLSAP_UPLOAD_GOODSMOV_3 complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="ArrayOfZALLSAP_UPLOAD_GOODSMOV_3"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ZALLSAP_UPLOAD_GOODSMOV_3" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ZALLSAP_UPLOAD_GOODSMOV_3" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfZALLSAP_UPLOAD_GOODSMOV_3", propOrder = {
    "zallsapuploadgoodsmov3"
})
public class ArrayOfZALLSAPUPLOADGOODSMOV3 {

    @XmlElement(name = "ZALLSAP_UPLOAD_GOODSMOV_3")
    protected List<ZALLSAPUPLOADGOODSMOV3> zallsapuploadgoodsmov3;

    /**
     * Gets the value of the zallsapuploadgoodsmov3 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the zallsapuploadgoodsmov3 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getZALLSAPUPLOADGOODSMOV3().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ZALLSAPUPLOADGOODSMOV3 }
     * 
     * 
     */
    public List<ZALLSAPUPLOADGOODSMOV3> getZALLSAPUPLOADGOODSMOV3() {
        if (zallsapuploadgoodsmov3 == null) {
            zallsapuploadgoodsmov3 = new ArrayList<ZALLSAPUPLOADGOODSMOV3>();
        }
        return this.zallsapuploadgoodsmov3;
    }

}
