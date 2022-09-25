package com.fisk.dataaccess.dto;

import com.fisk.common.core.enums.flink.UploadWayEnum;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author JianWenYang
 */
@Configuration
@Data
public class FlinkConfigDTO {

    @Value("${flink-config.host}")
    public String host;

    @Value("${flink-config.port}")
    public String port;

    @Value("${flink-config.user}")
    public String user;

    @Value("${flink-config.password}")
    public String password;

    @Value("${flink-config.upload-path}")
    public String uploadPath;

    @Value("${flink-config.upload-way}")
    public UploadWayEnum uploadWay;

}

