package com.fisk.datamodel.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author JianWenYang
 */
@Configuration
public class GetConfigDTO {

    @Value("${spring.datasource.url}")
    public String url;

    @Value("${spring.datasource.username}")
    public String username;

    @Value("${spring.datasource.password}")
    public String password;

}
