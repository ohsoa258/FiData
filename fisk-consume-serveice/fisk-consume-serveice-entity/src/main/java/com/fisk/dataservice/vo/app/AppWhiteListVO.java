package com.fisk.dataservice.vo.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 应用白名单
 * @date 2023/6/6 18:38
 */
@Data
public class AppWhiteListVO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public Integer appId;

    /**
     * API状态 1启用、0禁用
     */
    @ApiModelProperty(value = "apiState")
    public Integer apiState;

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
     * 是否开启授权认证 1开启 0关闭
     */
    @ApiModelProperty(value = "是否开启授权认证 1开启 0关闭")
    private int proxyAuthorizationSwitch;
}
