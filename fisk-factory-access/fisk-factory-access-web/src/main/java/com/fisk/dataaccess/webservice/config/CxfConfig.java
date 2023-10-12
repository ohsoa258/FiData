package com.fisk.dataaccess.webservice.config;

import com.fisk.dataaccess.webservice.IWebServiceServer;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.xml.ws.Endpoint;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该配置类用于创建(发布)webService客户端
 */
@Configuration
public class CxfConfig {

    @Resource(type = IWebServiceServer.class)
    private IWebServiceServer webServiceServer;

    /**
     * 注入Servlet 注意beanName不能为dispatcherServlet
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean(new CXFServlet(), "/webservice/*");
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }


    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), webServiceServer);
        endpoint.publish("/fidata-api");
        return endpoint;
    }
}
