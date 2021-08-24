package com.fisk.dataaccess.config;

import com.fisk.dataaccess.client.DataAccessClient;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Configuration
public class DataAccessFeignAutoConfiguration {

    @Resource
    DataAccessClient client;
}
