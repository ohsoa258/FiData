package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据安全-智能发现-发送结果的通知方式
 * @date 2023/2/1 12:04
 */
@Data
@TableName("tb_Intelligentdiscovery_notice")
public class IntelligentDiscovery_NoticePO extends BasePO {
    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    public int ruleId;

    /**
     * 扫描结果接收方式：1 邮件通知、2 站内通知、3 微信通知、4 短信通知
     */
    public int scanReceptionType;

    /**
     * 邮件配置表id
     */
    public int emailServerId;

    /**
     * 邮件主题
     */
    public String subject;

    /**
     * 正文
     */
    public String body;
}
