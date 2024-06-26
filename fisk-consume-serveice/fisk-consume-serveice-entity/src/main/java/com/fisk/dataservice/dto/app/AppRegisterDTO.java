package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;


/**
 * @author dick
 * @version v1.0
 * @description 应用 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class AppRegisterDTO
{
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appName;

    /**
     * 应用描述
     */
    @ApiModelProperty(value = "应用描述")
    @Length(min = 0, max = 255, message = "长度最多255")
    public String appDesc;

    /**
     * 应用申请人
     */
    @ApiModelProperty(value = "应用申请人")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appPrincipal;

    /**
     * 应用账号
     */
    @ApiModelProperty(value = "应用账号")
    public String appAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    public String appPassword;

    /**
     * 应用类型：1本地应用、2代理应用
     */
    @ApiModelProperty(value = "应用类型：1本地应用、2代理应用")
    public int appType;

    /**
     * 应用白名单，多个逗号分隔
     */
    @ApiModelProperty(value = "应用白名单，多个逗号分隔")
    public String appWhiteList;

    /**
     * 应用白名单状态：1启用、2禁用
     */
    @ApiModelProperty(value = "应用白名单状态：1启用、2禁用")
    public int appWhiteListState;

    /**
     * 部门名称
     */
    @ApiModelProperty(value = "部门名称")
    public String departName;


    /**
     * 是否开启授权认证 1开启 0关闭
     */
    @ApiModelProperty(value = "是否开启授权认证 1开启 0关闭")
    private int proxyAuthorizationSwitch;
}
