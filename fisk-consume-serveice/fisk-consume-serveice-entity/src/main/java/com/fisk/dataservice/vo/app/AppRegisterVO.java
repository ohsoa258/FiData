package com.fisk.dataservice.vo.app;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description 应用 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class AppRegisterVO
{
    /**
     * Id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;

    /**
     * 应用描述
     */
    @ApiModelProperty(value = "应用描述")
    public String appDesc;

    /**
     * 应用申请人
     */
    @ApiModelProperty(value = "应用申请人")
    public String appPrincipal;

    /**
     * 应用账号
     */
    @ApiModelProperty(value = "应用账号")
    public String appAccount;

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
     * 密码/加密
     */
    @ApiModelProperty(value = "密码/加密")
    public String appPassword;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 部门名称
     */
    @ApiModelProperty(value = "部门名称")
    public String departName;

    /**
     * 所有应用下服务总数
     */
    @ApiModelProperty(value = "所有应用下服务总数")
    public int totalCount;

    /**
     * 单个应用下服务个数
     */
    @ApiModelProperty(value = "单个应用下服务个数")
    public int  itemCount;


    /**
     * 是否开启授权认证 1开启 0关闭
     */
    @ApiModelProperty(value = "是否开启授权认证 1开启 0关闭")
    private int proxyAuthorizationSwitch;
}
