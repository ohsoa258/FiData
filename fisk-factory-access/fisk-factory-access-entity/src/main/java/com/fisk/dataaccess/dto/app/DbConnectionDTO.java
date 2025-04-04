package com.fisk.dataaccess.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DbConnectionDTO {
    /**
     * 驱动类型
     */
    @ApiModelProperty(value = "驱动类型", required = true)
    public String driveType;

    /**
     * 主机名
     */
    @ApiModelProperty(value = "服务器地址", required = true)
    public String host;

    /**
     * 端口号
     */
    @ApiModelProperty(value = "端口", required = true)
    public String port;

    /**
     * 数据库名
     */
    @ApiModelProperty(value = "数据库(ftp数据源没有数据库)", required = true)
    public String dbName;

    /**
     * 连接字符串
     */
    @ApiModelProperty(value = "连接字符串", required = true)
    public String connectStr;

    /**
     * 连接账号
     */
    @ApiModelProperty(value = "连接账号", required = true)
    public String connectAccount;

    /**
     * 连接密码
     */
    @ApiModelProperty(value = "连接密码", required = true)
    public String connectPwd;

    /**
     * JCO_SYSNR
     * 如果是mongodb  这个值就是验证数据库的数据库名称
     */
    @ApiModelProperty(value = "JCO_SYSNR", required = true)
    public String sysNr;

    /**
     * JCO_LANG
     *
     */
    @ApiModelProperty(value = "JCO_LANG", required = true)
    public String lang;

    /**
     * powerbi应用客户端id
     */
    @ApiModelProperty(value = "powerbi应用客户端id")
    public String powerbiClientId;

    /**
     * powerbi租户唯一标识符
     */
    @ApiModelProperty(value = "powerbi租户唯一标识符")
    public String powerbiClientSecret;

    /**
     * powerbi租户唯一标识符
     */
    @ApiModelProperty(value = "powerbi租户唯一标识符")
    public String powerbiTenantId;

}
