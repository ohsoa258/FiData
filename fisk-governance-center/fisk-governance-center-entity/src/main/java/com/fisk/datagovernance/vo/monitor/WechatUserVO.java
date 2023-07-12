package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WechatUserVO {

    @ApiModelProperty(value = "企业微信用户Id")
    public String wechatUserId;

    @ApiModelProperty(value = "企业微信用户名称")
    public String wechatUserName;
}
