package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
/**
 * @author cfk
 */
@Data
public class BuildGetFTPProcessorDTO extends BaseProcessorDTO {
    /*
     * ip
     * */
    @ApiModelProperty(value = "主机名")
    public String hostname;
    /*
     * 端口号
     * */
    @ApiModelProperty(value = "端口号")
    public String port;

    @ApiModelProperty(value = "用户名")
    public String username;
    /*
     * 密码
     * */
    @ApiModelProperty(value = "密码")
    public String password;
    /*
     * 文件路径
     * */
    @ApiModelProperty(value = "文件路径")
    public String remotePath;
    /*
     * 文件名称,加扩展名(全名称)
     * */
    @ApiModelProperty(value = "文件名称,加扩展名(全名称)")
    public String fileFilterRegex;
    /*
     * 是否启用utf-8
     * */
    @ApiModelProperty(value = "ftpUseUtf8")
    public boolean ftpUseUtf8;
}
