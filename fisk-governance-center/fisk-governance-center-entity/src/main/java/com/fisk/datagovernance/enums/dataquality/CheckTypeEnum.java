package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 校验类型
 * @date 2022/3/22 14:01
 */
public enum CheckTypeEnum implements BaseEnum {
    /**
     * 校验规则类型
     */
    NONE(0,"空"),
    UNIQUE_CHECK(1, "唯一校验"),
    NONEMPTY_CHECK(2, "非空校验"),
    DATA_CHECK(3, "数据校验");

    CheckTypeEnum(int value, String name) {
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

    public static CheckTypeEnum getEnum(int value){
        for (CheckTypeEnum e: CheckTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return CheckTypeEnum.NONE;
    }

    public static CheckTypeEnum getEnumByName(String name){
        for (CheckTypeEnum e: CheckTypeEnum.values()) {
            if(e.getName().equals(name))
                return e;
        }
        return CheckTypeEnum.NONE;
    }
}
