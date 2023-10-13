package com.fisk.dataservice.handler.webservice.factory;

import com.fisk.dataservice.handler.webservice.WebServiceHandler;
import com.fisk.dataservice.handler.webservice.impl.WebServiceBasicValidation;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
public class InterfaceWebServiceFactory {
    public static WebServiceHandler getWebServiceHandlerByType() {
        return new WebServiceBasicValidation();
    }
}
