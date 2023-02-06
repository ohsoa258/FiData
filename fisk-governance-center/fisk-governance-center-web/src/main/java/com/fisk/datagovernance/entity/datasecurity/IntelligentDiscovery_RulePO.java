package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据安全-智能发现-规则配置PO
 * @date 2023/2/1 12:04
 */
@Data
@TableName("tb_Intelligentdiscovery_rule")
public class IntelligentDiscovery_RulePO extends BasePO {
    /**
     * 规则名称
     */
    public String ruleName;

    /**
     * 规则类型：1 关键字、2 正则表达式
     */
    public int ruleType;

    /**
     * 规则值：序列化Json的关键字规则 OR 正则表达式
     */
    public String ruleValue;

    /**
     * 规则描述
     */
    public String ruleDescribe;

    /**
     * 规则状态：1 启用、0 禁用
     */
    public int ruleState;

    /**
     * 风险等级：L1~L3，L1为低风险、L2为中风险、L3为高风险
     */
    public String riskLevel;

    /**
     * 扫描周期，Cron表达式
     */
    public String scanPeriod;

    /**
     * 扫描风险数量
     */
    public int scanRiskCount;

    /**
     * 负责人，默认取值当前登录用户
     */
    public String principal;
}
