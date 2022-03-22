package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 校验规则类型
 * @date 2022/3/22 14:01
 */
public enum CheckRuleTypeEnum implements BaseEnum {
    /**
     * 校验规则类型
     */
    UNIQUE_CHECK(1, "唯一校验"),
    NONEMPTY_CHECK(2, "非空校验"),
    LENGTH_CHECK(3, "长度校验");

    CheckRuleTypeEnum(int value, String name) {
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
}
