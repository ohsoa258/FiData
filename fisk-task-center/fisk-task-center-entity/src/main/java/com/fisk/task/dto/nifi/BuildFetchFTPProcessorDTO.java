package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildFetchFTPProcessorDTO extends BaseProcessorDTO {
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
    @ApiModelProperty(value = "ftpUseUtf8")
    public boolean ftpUseUtf8;
}
