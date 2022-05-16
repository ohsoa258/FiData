package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 通知扩展表
 * @date 2022/3/22 15:20
 */
@Data
@TableName("tb_notice_rule_extend")
public class NoticeExtendPO extends BasePO {
    /**
     * 通知id
     */
    public int noticeId;

    /**
     * 模块类型
     * 100、数据校验 200、业务清洗
     * 300、生命周期
     */
    public int moduleType;

    /**
     * 规则id
     */
    public int ruleId;
}
