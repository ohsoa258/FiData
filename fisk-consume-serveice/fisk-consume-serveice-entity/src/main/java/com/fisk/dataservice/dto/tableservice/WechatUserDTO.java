package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 企业微信用户DTO
 * @date 2023/6/21 13:46
 */
@Data
public class WechatUserDTO {
    /**
     * 企业微信用户Id
     */
    @ApiModelProperty(value = "企业微信用户Id")
    public String wechatUserId;

    /**
     * 企业微信用户名称
     */
    @ApiModelProperty(value = "企业微信用户名称")
    public String wechatUserName;
}
