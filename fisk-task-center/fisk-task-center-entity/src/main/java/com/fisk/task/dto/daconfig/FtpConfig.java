package com.fisk.task.dto.daconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * ftp配置项
 *
 * @author cfk
 */
@Data
public class FtpConfig {
    /*
    * ip
    * */
    @ApiModelProperty(value = "主机名")
    public String hostname;
    /*
     * 端口号
     * */
    @ApiModelProperty(value = "端口")
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
    @ApiModelProperty(value = "是否启用utf-8")
    public boolean ftpUseUtf8;
    /*
     * 第一个sheet页名称
     * */
    @ApiModelProperty(value = "第一个sheet页名称")
    public String sheetName;

    /**
     * excel 开始读取数据行数
     */
    @ApiModelProperty(value = "excel 开始读取数据行数")
    public Integer startLine;

    /**
     * 上传Linux秘钥地址
     */
    @ApiModelProperty(value = "上传Linux秘钥地址")
    public String linuxPath;

    /**
     * sftp秘钥文件民
     */
    @ApiModelProperty(value = "sftp秘钥文件民")
    public String fileName;

    /**
     * sftp秘钥二进制数据
     */
    @ApiModelProperty(value = "sftp秘钥二进制数据")
    public String fileBinary;

    /**
     * 是否是sftp
     */
    @ApiModelProperty(value = "是否是sftp")
    public boolean whetherSftpl;


}
