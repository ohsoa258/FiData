package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_LogsVO {
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * uuid，关联tb_attachmentInfo表查询附件信息
     */
    @ApiModelProperty(value = "uuid，关联tb_attachmentInfo表查询附件信息")
    public String uniqueId;

    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    @ApiModelProperty(value = "tb_Intelligentdiscovery_rule表主键ID")
    public int ruleId;

    /**
     * 报告（规则）名称
     */
    @ApiModelProperty(value = "报告（规则）名称")
    public String ruleName;

    /**
     * 扫描结果接收方式：邮件通知、站内通知、微信通知、短信通知
     */
    @ApiModelProperty(value = "扫描结果接收方式：邮件通知、站内通知、微信通知、短信通知")
    public String scanReceptionTypeName;

    /**
     * 扫描风险数量
     */
    @ApiModelProperty(value = "扫描风险数量")
    public int scanRiskCount;

    /**
     * 发送时间
     */
    @ApiModelProperty(value = "发送时间")
    public String sendTime;

    /**
     * 发送结果
     */
    @ApiModelProperty(value = "发送结果")
    public String sendResult;

    /**
     * 接收人邮箱，逗号分割
     */
    @ApiModelProperty(value = "接收人邮箱，逗号分割")
    public String recipientEmails;

    /**
     * 是否存在报告
     */
    @ApiModelProperty(value = "是否存在报告")
    public boolean existReport;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "报告名称")
    public String originalName;
}
