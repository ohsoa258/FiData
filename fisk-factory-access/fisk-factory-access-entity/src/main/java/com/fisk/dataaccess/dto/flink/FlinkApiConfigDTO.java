package com.fisk.dataaccess.dto.flink;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author JianWenYang
 */
@Configuration
@Data
public class FlinkApiConfigDTO {

    @Value("${flink-api-config.host}")
    public String host;

    @Value("${flink-api-config.port}")
    public Integer port;

    @Value("${flink-api-config.savepoint-status}")
    public String savepointStatus;

    @Value("${flink-api-config.savepoints}")
    public String savepoints;

}
