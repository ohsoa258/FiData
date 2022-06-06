package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 校验规则
 * @date 2022/5/18 19:59
 */
public enum CheckRuleEnum implements BaseEnum {
    /**
     * 校验规则
     */
    NONE(0,"空"),
    STRONG_RULE(1, "强规则"),
    WEAK_RULE(2, "弱规则");

    CheckRuleEnum(int value, String name) {
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

    public static CheckRuleEnum  getEnum(int value){
        for (CheckRuleEnum e:CheckRuleEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return CheckRuleEnum.NONE;
    }
}
