package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_NoticeVO {
    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    @ApiModelProperty(value = "tb_Intelligentdiscovery_rule表主键ID")
    public int ruleId;

    /**
     * 扫描结果接收方式：1 邮件通知、2 站内通知、3 微信通知、4 短信通知
     */
    @ApiModelProperty(value = "扫描结果接收方式：1 邮件通知、2 站内通知、3 微信通知、4 短信通知")
    public int scanReceptionType;

    /**
     * 邮件配置表id
     */
    @ApiModelProperty(value = "邮件配置表id")
    public int emailServerId;

    /**
     * 邮件主题
     */
    @ApiModelProperty(value = "邮件主题")
    public String subject;

    /**
     * 正文
     */
    @ApiModelProperty(value = "正文")
    public String body;
}
