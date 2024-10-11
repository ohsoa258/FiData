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
     * 启用状态 1、启用 2、禁用
     */
    @ApiModelProperty(value = "启用状态")
    public int enable;

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

    /**
     * 告警条件：1 成功、2 失败、3 完成
     */
    @ApiModelProperty(value = "告警条件：1 成功、2 失败、3 完成")
    public int alarmConditions;


    /**
     * 预警级别(EmailWarnLevelEnum)
     */
    public Integer warnLevel;
}
