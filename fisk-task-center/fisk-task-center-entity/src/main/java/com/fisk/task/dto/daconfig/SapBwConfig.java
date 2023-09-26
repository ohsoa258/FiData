package com.fisk.task.dto.daconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * SapBw配置项
 *
 * @author 李世纪
 */
@Data
public class SapBwConfig {

    /**
     * tb_app_registration表id  应用id
     */
    @ApiModelProperty(value = "tb_app_registration表id  应用id")
    public long appId;

    /**
     * tb_app_drivetype表type  驱动类型
     */
    @ApiModelProperty(value = "tb_app_drivetype表type  驱动类型")
    public String driveType;

    /**
     * 主机名 serverHost
     */
    @ApiModelProperty(value = "主机名 serverHost")
    public String host;

    /**
     * 客户端号
     */
    @ApiModelProperty(value = "客户端号")
    public String port;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    public String connectAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    public String connectPwd;

    /**
     * JCO_SYSNR
     */
    @ApiModelProperty(value = "JCO_SYSNR")
    public String sysNr;

    /**
     * JCO_LANG
     */
    @ApiModelProperty(value = "JCO_LANG")
    public String lang;

    /**
     * mdx语句
     */
    @ApiModelProperty(value = "mdx语句")
    public String mdxSql;

    @ApiModelProperty(value = "sapbw-mdx语句集合", required = true)
    public List<String> mdxList;

}
