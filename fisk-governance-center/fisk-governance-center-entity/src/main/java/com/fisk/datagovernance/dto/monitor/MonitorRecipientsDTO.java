package com.fisk.datagovernance.dto.monitor;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;
@Data
public class MonitorRecipientsDTO {

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

    /**
     * 预警级别(EmailWarnLevelEnum)
     */
    @ApiModelProperty(value = "预警级别(EmailWarnLevelEnum)")
    public Integer warnLevel;
}
