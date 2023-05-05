package com.fisk.dataaccess.dto.flink;

import com.fisk.common.core.enums.flink.CommandEnum;
import com.fisk.common.core.enums.flink.UploadWayEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author JianWenYang
 */
@Configuration
@Data
public class FlinkConfigDTO {

    @ApiModelProperty(value = "主机")
    @Value("${flink-config.host}")
    public String host;

    @ApiModelProperty(value = "端口")
    @Value("${flink-config.port}")
    public Integer port;

    @ApiModelProperty(value = "用户")
    @Value("${flink-config.user}")
    public String user;

    @ApiModelProperty(value = "密码")
    @Value("${flink-config.password}")
    public String password;

    @ApiModelProperty(value = "上传路径")
    @Value("${flink-config.upload-path}")
    public String uploadPath;

    @ApiModelProperty(value = "上传方式")
    @Value("${flink-config.upload-way}")
    public UploadWayEnum uploadWay;

    @ApiModelProperty(value = "保存点路径")
    @Value("${flink-config.savepoints-path}")
    public String savePointsPath;

    @ApiModelProperty(value = "命令枚举")
    @Value("${flink-config.command}")
    public CommandEnum commandEnum;

    @ApiModelProperty(value = "命令路径")
    @Value("${flink-config.command-path}")
    public String commandPath;

    @ApiModelProperty(value = "文件名")
    public String fileName;

}

