/**
 * IASRSSAPServiceserviceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fisk.dataservice.test;

public class IASRSSAPServiceserviceLocator extends org.apache.axis.client.Service implements IASRSSAPServiceservice {

    public IASRSSAPServiceserviceLocator() {
    }


    public IASRSSAPServiceserviceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public IASRSSAPServiceserviceLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for IASRSSAPServicePort
    private String IASRSSAPServicePort_address = "http://localhost:8080/soap/IASRSSAPService";

    public String getIASRSSAPServicePortAddress() {
        return IASRSSAPServicePort_address;
    }

    // The WSDD service name defaults to the port name.
    private String IASRSSAPServicePortWSDDServiceName = "IASRSSAPServicePort";

    public String getIASRSSAPServicePortWSDDServiceName() {
        return IASRSSAPServicePortWSDDServiceName;
    }

    public void setIASRSSAPServicePortWSDDServiceName(String name) {
        IASRSSAPServicePortWSDDServiceName = name;
    }

    public IASRSSAPService getIASRSSAPServicePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(IASRSSAPServicePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getIASRSSAPServicePort(endpoint);
    }

    public IASRSSAPService getIASRSSAPServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            IASRSSAPServicebindingStub _stub = new IASRSSAPServicebindingStub(portAddress, this);
            _stub.setPortName(getIASRSSAPServicePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setIASRSSAPServicePortEndpointAddress(String address) {
        IASRSSAPServicePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (IASRSSAPService.class.isAssignableFrom(serviceEndpointInterface)) {
                IASRSSAPServicebindingStub _stub = new IASRSSAPServicebindingStub(new java.net.URL(IASRSSAPServicePort_address), this);
                _stub.setPortName(getIASRSSAPServicePortWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("IASRSSAPServicePort".equals(inputPortName)) {
            return getIASRSSAPServicePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "IASRSSAPServiceservice");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "IASRSSAPServicePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {
        
if ("IASRSSAPServicePort".equals(portName)) {
            setIASRSSAPServicePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
