package com.fisk.dataaccess.webservice.config;

import com.fisk.dataaccess.webservice.IServerAcknowledgement;
import com.fisk.dataaccess.webservice.IServerInventoryStatus;
import com.fisk.dataaccess.webservice.IServerItemData;
import com.fisk.dataaccess.webservice.IWebServiceServer;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.xml.ws.Endpoint;
import java.util.Collections;
import java.util.List;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该配置类用于创建(发布)webService客户端
 */
@Configuration
public class CxfConfig {

    @Resource(type = IWebServiceServer.class)
    private IWebServiceServer webServiceServer;

    @Resource(type = IServerItemData.class)
    private IServerItemData serverItemData;

    @Resource(type = IServerInventoryStatus.class)
    private IServerInventoryStatus serverInventoryStatus;

    @Resource(type = IServerAcknowledgement.class)
    private IServerAcknowledgement serverAcknowledgement;

    /**
     * 注入Servlet 注意beanName不能为dispatcherServlet
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean(new CXFServlet(), "/FISK/*");
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }


    @Bean
    @Qualifier("endpoint")
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), webServiceServer);
        endpoint.getOutInterceptors().add(new XmlnsInterceptor(Phase.PRE_STREAM));
        endpoint.publish("/KsfNotice");
        return endpoint;
    }

    @Bean
    @Qualifier("endpoint1")
    public Endpoint endpoint1() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), serverItemData);
        endpoint.publish("/ItemData");
        return endpoint;
    }

    @Bean
    @Qualifier("endpoint2")
    public Endpoint endpoint2() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), serverInventoryStatus);
        endpoint.publish("/InventoryStatus");
        return endpoint;
    }

    @Bean
    @Qualifier("endpoint3")
    public Endpoint endpoint3() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), serverAcknowledgement);
        endpoint.publish("/Acknowledgement");
        return endpoint;
    }

}
