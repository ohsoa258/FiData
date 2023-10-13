package com.fisk.dataaccess.test;

import com.alibaba.fastjson.JSON;
import com.fisk.dataaccess.webservice.service.WebServiceReceiveDataDTO;
import com.fisk.dataaccess.webservice.service.WebServiceUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

@Slf4j
public class WebServiceDemo {

    /**
     * FiData java code example
     * Third-party companies call FiData webService to push data interface by Java code
     *
     * @Date 2023/10/13
     * @Version 1.0
     */
    public static void main(String[] args) {
        Client client = null;
        try {
            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            client = dcf.createClient("{web_service_address}");

            // config HTTPConduit
            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setAllowChunking(false);
            // ConnectionTimeout 60 seconds
            policy.setConnectionTimeout(60000);
            // ReceiveTimeout 60 seconds
            policy.setReceiveTimeout(60000);
            conduit.setClient(policy);

            // webServiceGetToken - variable
            WebServiceUserDTO webServiceUserDTO = new WebServiceUserDTO();
            webServiceUserDTO.setPassword("pwd");
            webServiceUserDTO.setUseraccount("username");
            // invoke methods : webServiceGetToken
            Object[] objects = client.invoke("webServiceGetToken", webServiceUserDTO);
            // webServiceGetToken receive data : token
            String token = objects[0].toString();

            // webServicePushData - variable
            WebServiceReceiveDataDTO webServiceReceiveDataDTO = new WebServiceReceiveDataDTO();
            // WebServiceCode
            webServiceReceiveDataDTO.setWebServiceCode(123L);
            // your Json data
            webServiceReceiveDataDTO.setData("your Json data");
            // token
            webServiceReceiveDataDTO.setToken(token);
            // Start calling method - invoke("method name", parameter 1, parameter 2, parameter 3....);
            Object[] result = client.invoke("webServicePushData", webServiceReceiveDataDTO);
            // webServicePushData receive data : result
            String msg = JSON.toJSONString(result);
            // print result or do sth else
            System.out.println(msg);
        } catch (Exception e) {
            log.error("webService error--" + e);
        }finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    log.error("webService error--" + e);
                }
            }
        }
    }

}
