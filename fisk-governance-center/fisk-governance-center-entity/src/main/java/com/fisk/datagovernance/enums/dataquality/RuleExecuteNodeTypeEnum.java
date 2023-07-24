package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 规则执行节点类型
 * @date 2022/5/31 16:33
 */
public enum RuleExecuteNodeTypeEnum implements BaseEnum {

    NONE(0, "空"),
    BEFORE_SYNCHRONIZATION(1, "同步前"),
    SYNCHRONIZATION(2, "同步中"),
    AFTER_SYNCHRONIZATION(3, "同步后");
    
    RuleExecuteNodeTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final int value;
    private final String name;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }


    public static RuleExecuteNodeTypeEnum getEnum(int value) {
        for (RuleExecuteNodeTypeEnum e : RuleExecuteNodeTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return RuleExecuteNodeTypeEnum.NONE;
    }

    public static RuleExecuteNodeTypeEnum getEnumByName(String name) {
        for (RuleExecuteNodeTypeEnum e : RuleExecuteNodeTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return RuleExecuteNodeTypeEnum.NONE;
    }
}
