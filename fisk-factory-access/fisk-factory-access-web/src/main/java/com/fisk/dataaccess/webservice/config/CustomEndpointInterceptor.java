//package com.fisk.dataaccess.webservice.config;
//
//import org.springframework.ws.context.MessageContext;
//import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
//import org.springframework.ws.soap.saaj.SaajSoapMessage;
//
//import javax.xml.soap.*;
//import java.util.Iterator;
//
//public class CustomEndpointInterceptor extends EndpointInterceptorAdapter {
//
//    private static final String SOAP_ENV_NAMESPACE = "http://ws.webservices.services.adapter.datahub/";
//    private static final String PREFERRED_PREFIX = "";
//
//
//    @Override
//    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
//        SaajSoapMessage soapResponse = (SaajSoapMessage) messageContext.getResponse();
//        alterSoapEnvelope(soapResponse);
//        return super.handleResponse(messageContext, endpoint);
//    }
//
//
//    private void alterSoapEnvelope(SaajSoapMessage soapResponse) {
//        try {
//            SOAPMessage soapMessage = soapResponse.getSaajMessage();
//            SOAPPart soapPart = soapMessage.getSOAPPart();
//            SOAPEnvelope envelope = soapPart.getEnvelope();
//            SOAPBody body = soapMessage.getSOAPBody();
//            envelope.addNamespaceDeclaration(PREFERRED_PREFIX, SOAP_ENV_NAMESPACE);
//
//            Iterator<SOAPBodyElement> it = body.getChildElements();
//            while (it.hasNext()) {
//                SOAPBodyElement node = it.next();
//                node.removeNamespaceDeclaration("");
//                node.removeNamespaceDeclaration(node.getPrefix());
//                node.setPrefix(PREFERRED_PREFIX);
//                Iterator<SOAPBodyElement> it2 = node.getChildElements();
//                while (it2.hasNext()) {
//                    SOAPBodyElement node2 = it2.next();
//                    node2.setPrefix("");
//                }
//            }
//            soapMessage.saveChanges();
//        } catch (SOAPException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}