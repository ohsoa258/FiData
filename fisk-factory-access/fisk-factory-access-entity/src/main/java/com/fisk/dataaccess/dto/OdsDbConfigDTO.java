package com.fisk.dataaccess.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author JianWenYang
 */
@Configuration
@Data
public class OdsDbConfigDTO {

    @Value("${pgsql-ods.url}")
    public String url;

    @Value("${pgsql-ods.username}")
    public String username;

    @Value("${pgsql-ods.password}")
    public String password;
}
