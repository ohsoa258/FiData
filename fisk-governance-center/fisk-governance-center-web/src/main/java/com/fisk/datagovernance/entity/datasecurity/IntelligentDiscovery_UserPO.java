package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据安全-智能发现-扫描结果接收人PO
 * @date 2023/2/1 12:04
 */
@Data
@TableName("tb_Intelligentdiscovery_user")
public class IntelligentDiscovery_UserPO extends BasePO {
    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    public int ruleId;

    /**
     * 接收人名称
     */
    public String recipientName;

    /**
     * 接收人邮箱
     */
    public String recipientEmail;
}
