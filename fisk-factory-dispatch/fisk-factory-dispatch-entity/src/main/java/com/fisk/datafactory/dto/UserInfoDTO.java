package com.fisk.datafactory.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class UserInfoDTO {
    @ApiModelProperty(value = "企业微信用户名称")
    public String wechatUserName;
    /**
     * 企业微信用户Id
     */
    @ApiModelProperty(value = "企业微信用户Id")
    public String wechatUserId;
    /**
     * 用户名称
     */
    @ApiModelProperty(value = "用户名称")
    public String userId;
    /**
     * 用户Id
     */
    @ApiModelProperty(value = "用户Id")
    public String usetName;
}
