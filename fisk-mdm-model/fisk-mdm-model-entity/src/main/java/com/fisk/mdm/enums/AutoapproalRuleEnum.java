package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 自动审批规则
 */
public enum AutoapproalRuleEnum implements BaseEnum {

    /**
     * 自动审批规则
     */
    ONLY_ONE_RULE(1, "仅首个节点需审批，其余自动同意"),

    CONTINUOUS_APPROVAL_RULE(2, "仅连续审批时自动同意"),

    ALL_APPROVAL_RULE(3, "每个节点都需要审批");
    @EnumValue
    private final int value;
    private final String name;

    AutoapproalRuleEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}