package com.fisk.dataaccess.dto.flink;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author JianWenYang
 */
@Configuration
@Data
public class FlinkApiConfigDTO {

    @ApiModelProperty(value = "主机")
    @Value("${flink-api-config.host}")
    public String host;

    @ApiModelProperty(value = "端口")
    @Value("${flink-api-config.port}")
    public Integer port;

    @ApiModelProperty(value = "保持状态")
    @Value("${flink-api-config.savepoint-status}")
    public String savepointStatus;

    @ApiModelProperty(value = "保存点")
    @Value("${flink-api-config.savepoints}")
    public String savepoints;

}
