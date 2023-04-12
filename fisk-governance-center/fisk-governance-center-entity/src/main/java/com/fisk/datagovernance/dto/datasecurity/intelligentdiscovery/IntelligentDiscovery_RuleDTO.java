package com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntelligentDiscovery_RuleDTO {
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 规则类型：1 关键字、2 正则表达式
     */
    @ApiModelProperty(value = "规则类型：1 关键字、2 正则表达式")
    public int ruleType;

    /**
     * 规则值：序列化Json的关键字规则 OR 正则表达式
     */
    @ApiModelProperty(value = "规则值：序列化Json的关键字规则 OR 正则表达式")
    public String ruleValue;

    /**
     * 规则描述
     */
    @ApiModelProperty(value = "规则描述")
    public String ruleDescribe;

    /**
     * 规则状态：1 启用、0 禁用
     */
    @ApiModelProperty(value = "规则状态：1 启用、0 禁用")
    public int ruleState;

    /**
     * 风险等级：L1~L3，L1为低风险、L2为中风险、L3为高风险
     */
    @ApiModelProperty(value = "风险等级：L1~L3，L1为低风险、L2为中风险、L3为高风险")
    public String riskLevel;

    /**
     * 扫描周期，Cron表达式
     */
    @ApiModelProperty(value = "扫描周期，Cron表达式")
    public String scanPeriod;

    /**
     * 扫描风险数量
     */
    @ApiModelProperty(value = "扫描风险数量")
    public int scanRiskCount;

    /**
     * 负责人，默认取值当前登录用户
     */
    @ApiModelProperty(value = "负责人，默认取值当前登录用户")
    public String principal;

    /**
     * 智能发现-通知方式配置
     */
    @ApiModelProperty(value = "智能发现-通知方式配置")
    public IntelligentDiscovery_NoticeDTO notice;

    /**
     * 智能发现-关键字规则列表
     */
    @ApiModelProperty(value = "智能发现-关键字规则列表")
    public List<IntelligentDiscovery_KeyWordRuleDTO> keyWordRules;

    /**
     * 智能发现-扫描配置列表
     */
    @ApiModelProperty(value = "智能发现-扫描配置列表")
    public List<IntelligentDiscovery_ScanDTO> scans;

    /**
     * 智能发现-扫描结果接收人列表
     */
    @ApiModelProperty(value = "智能发现-扫描结果接收人列表")
    public List<IntelligentDiscovery_UserDTO> users;
}
