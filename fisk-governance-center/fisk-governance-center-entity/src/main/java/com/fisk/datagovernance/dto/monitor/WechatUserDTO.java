package com.fisk.datagovernance.dto.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class WechatUserDTO {

    @ApiModelProperty(value = "企业微信用户Id")
    public String wechatUserId;

    @ApiModelProperty(value = "企业微信用户名称")
    public String wechatUserName;
}
