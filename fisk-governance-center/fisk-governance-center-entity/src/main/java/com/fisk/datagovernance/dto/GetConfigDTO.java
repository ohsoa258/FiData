package com.fisk.datagovernance.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author dick
 * @version 1.0
 * @description 获取配置文件
 * @date 2022/11/29 10:57
 */
@Data
public class GetConfigDTO {
    @Value("${spring.datasource.url}")
    public String url;

    @Value("${spring.datasource.username}")
    public String username;

    @Value("${spring.datasource.password}")
    public String password;

    @Value("${spring.datasource.driver-class-name}")
    public String driver;
}
