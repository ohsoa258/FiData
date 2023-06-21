package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 表服务应用告警通知收件人
 * @date 2023/6/21 13:42
 */
@Data
public class TableRecipientsDTO {
    /**
     * 表服务应用Id
     */
    @ApiModelProperty(value = "表服务应用Id")
    public int tableAppId;

    /**
     * 通知服务器Id
     */
    @ApiModelProperty(value = "通知服务器Id")
    public int noticeServerId;

    /**
     * 通知服务类型：1、邮箱 2、企业微信
     */
    @ApiModelProperty(value = "通知服务类型：1、邮箱 2、企业微信")
    public int noticeServerType;

    /**
     * 企业微信用户列表
     */
    @ApiModelProperty(value = "企业微信用户列表")
    public List<WechatUserDTO> wechatUserList;

    /**
     * 用户邮箱，多个分号分隔
     */
    @ApiModelProperty(value = "用户邮箱，多个分号分隔")
    public String userEmails;
}
