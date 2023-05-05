package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildFetchSFTPProcessorDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "主机名")
    public String hostname;
    @ApiModelProperty(value = "端口")
    public String port;
    @ApiModelProperty(value = "用户名")
    public String username;
    @ApiModelProperty(value = "密码")
    public String password;
    @ApiModelProperty(value = "文件路径")
    public String remoteFile;
    /**
     * Send Keep Alive On Timeout
     *
     */
    @ApiModelProperty(value = "在超时时发送Keep Alive")
    public String sendKeepAliveOnTimeout;

    /**
     * Connection Timeout
     */
    @ApiModelProperty(value = "连接超时")
    public String connectionTimeout;

    /**
     * Data Timeout
     */
    @ApiModelProperty(value = "数据超时")
    public String dataTimeout;

    @ApiModelProperty(value = "私钥路径")
    public String privateKeyPath;


}
