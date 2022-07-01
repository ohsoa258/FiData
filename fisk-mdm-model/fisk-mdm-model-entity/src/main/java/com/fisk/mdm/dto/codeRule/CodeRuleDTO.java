package com.fisk.mdm.dto.codeRule;

import com.fisk.mdm.enums.RuleTypeEnum;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/1 11:09
 * @Version 1.0
 */
@Data
public class CodeRuleDTO {

    /**
     * 规则组id
     */
    private Integer groupId;

    /**
     * 排序
     */
    private Integer order;

    /**
     * 规则类型
     */
    private RuleTypeEnum ruleType;

    /**
     * 常量值
     */
    private String constValue;

    /**
     * 属性id
     */
    private Integer attributeId;

    /**
     * 时间格式
     */
    private String dateFormat;
}
