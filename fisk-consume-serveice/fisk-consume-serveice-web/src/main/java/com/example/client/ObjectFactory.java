
package com.example.client;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.example.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ZALLSAPUPLOADGOODSMOV2MENGE_QNAME = new QName("http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", "MENGE");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.example.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OperationZALLSAPUPLOADGOODSMOV }
     * 
     */
    public OperationZALLSAPUPLOADGOODSMOV createOperationZALLSAPUPLOADGOODSMOV() {
        return new OperationZALLSAPUPLOADGOODSMOV();
    }

    /**
     * Create an instance of {@link ZALLSAPUPLOADGOODSMOV }
     * 
     */
    public ZALLSAPUPLOADGOODSMOV createZALLSAPUPLOADGOODSMOV() {
        return new ZALLSAPUPLOADGOODSMOV();
    }

    /**
     * Create an instance of {@link ArrayOfZALLSAPUPLOADGOODSMOV2 }
     * 
     */
    public ArrayOfZALLSAPUPLOADGOODSMOV2 createArrayOfZALLSAPUPLOADGOODSMOV2() {
        return new ArrayOfZALLSAPUPLOADGOODSMOV2();
    }

    /**
     * Create an instance of {@link ArrayOfZALLSAPUPLOADGOODSMOV1 }
     * 
     */
    public ArrayOfZALLSAPUPLOADGOODSMOV1 createArrayOfZALLSAPUPLOADGOODSMOV1() {
        return new ArrayOfZALLSAPUPLOADGOODSMOV1();
    }

    /**
     * Create an instance of {@link ArrayOfZALLSAPUPLOADGOODSMOV3 }
     * 
     */
    public ArrayOfZALLSAPUPLOADGOODSMOV3 createArrayOfZALLSAPUPLOADGOODSMOV3() {
        return new ArrayOfZALLSAPUPLOADGOODSMOV3();
    }

    /**
     * Create an instance of {@link OperationZALLSAPUPLOADGOODSMOVResponse }
     * 
     */
    public OperationZALLSAPUPLOADGOODSMOVResponse createOperationZALLSAPUPLOADGOODSMOVResponse() {
        return new OperationZALLSAPUPLOADGOODSMOVResponse();
    }

    /**
     * Create an instance of {@link ZALLSAPUPLOADGOODSMOVResponse }
     * 
     */
    public ZALLSAPUPLOADGOODSMOVResponse createZALLSAPUPLOADGOODSMOVResponse() {
        return new ZALLSAPUPLOADGOODSMOVResponse();
    }

    /**
     * Create an instance of {@link ZALLSAPUPLOADGOODSMOV2 }
     * 
     */
    public ZALLSAPUPLOADGOODSMOV2 createZALLSAPUPLOADGOODSMOV2() {
        return new ZALLSAPUPLOADGOODSMOV2();
    }

    /**
     * Create an instance of {@link ZALLSAPUPLOADGOODSMOV1 }
     * 
     */
    public ZALLSAPUPLOADGOODSMOV1 createZALLSAPUPLOADGOODSMOV1() {
        return new ZALLSAPUPLOADGOODSMOV1();
    }

    /**
     * Create an instance of {@link ZALLSAPUPLOADGOODSMOV3 }
     * 
     */
    public ZALLSAPUPLOADGOODSMOV3 createZALLSAPUPLOADGOODSMOV3() {
        return new ZALLSAPUPLOADGOODSMOV3();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     */
    @XmlElementDecl(namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", name = "MENGE", scope = ZALLSAPUPLOADGOODSMOV2 .class)
    public JAXBElement<BigDecimal> createZALLSAPUPLOADGOODSMOV2MENGE(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_ZALLSAPUPLOADGOODSMOV2MENGE_QNAME, BigDecimal.class, ZALLSAPUPLOADGOODSMOV2 .class, value);
    }

}
