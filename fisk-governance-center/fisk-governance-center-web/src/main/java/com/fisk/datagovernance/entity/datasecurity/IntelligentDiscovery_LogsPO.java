package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据安全-智能发现-规则执行日志PO
 * @date 2023/2/1 12:03
 */
@Data
@TableName("tb_Intelligentdiscovery_logs")
public class IntelligentDiscovery_LogsPO extends BasePO {
    /**
     * uuid，关联tb_attachmentInfo表查询附件信息
     */
    public String uniqueId;

    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    public int ruleId;

    /**
     * 报告（规则）名称
     */
    public String ruleName;

    /**
     * 扫描结果接收方式：邮件通知、站内通知、微信通知、短信通知
     */
    public String scanReceptionTypeName;

    /**
     * 扫描风险数量
     */
    public int scanRiskCount;

    /**
     * 发送时间
     */
    public String sendTime;

    /**
     * 发送结果
     */
    public String sendResult;

    /**
     * 接收人邮箱，逗号分割
     */
    public String recipientEmails;
}
