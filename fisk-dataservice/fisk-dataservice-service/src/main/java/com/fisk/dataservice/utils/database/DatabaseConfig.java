package com.fisk.dataservice.utils.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author WangYan
 * @date 2021/12/9 17:55
 */
@Component
public class DatabaseConfig {

    public static String url;
    public static String username;
    public static String password;
    public static String driver;

    @Value("${databaseConfig.url}")
    public void setUrl(String url) { DatabaseConfig.url = url; }

    @Value("${databaseConfig.username}")
    public void setUsername(String username) { DatabaseConfig.username = username; }

    @Value("${databaseConfig.password}")
    public void setPassword(String password) { DatabaseConfig.password = password; }

    @Value("${databaseConfig.driver}")
    public void setDriver(String driver) { DatabaseConfig.driver = driver; }
}
