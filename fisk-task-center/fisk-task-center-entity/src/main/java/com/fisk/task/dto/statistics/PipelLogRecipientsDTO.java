package com.fisk.task.dto.statistics;

import com.fisk.dataservice.dto.tableservice.WechatUserDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-24
 * @Description:
 */
@Data
public class PipelLogRecipientsDTO {
    public String cronExpression;

    @ApiModelProperty(value = "通知服务器Id")
    public int noticeServerId;

    @ApiModelProperty(value = "通知服务类型：1、邮箱 2、企业微信")
    public int noticeServerType;

    @ApiModelProperty(value = "启用状态")
    public int enable;

    @ApiModelProperty(value = "企业微信用户列表")
    public List<WechatUserDTO> wechatUserList;

    @ApiModelProperty(value = "用户邮箱，多个分号分隔")
    public String userEmails;
}
