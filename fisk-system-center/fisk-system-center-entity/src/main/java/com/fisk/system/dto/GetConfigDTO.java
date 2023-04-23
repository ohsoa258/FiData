package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author JianWenYang
 */
@Configuration
@Data
public class GetConfigDTO {

    @ApiModelProperty(value = "路径")
    @Value("${spring.datasource.url}")
    public String url;

    @ApiModelProperty(value = "用户名称")
    @Value("${spring.datasource.username}")
    public String username;

    @ApiModelProperty(value = "密码")
    @Value("${spring.datasource.password}")
    public String password;

    @ApiModelProperty(value = "驱动")
    @Value("${spring.datasource.driver-class-name}")
    public String driver;

}
