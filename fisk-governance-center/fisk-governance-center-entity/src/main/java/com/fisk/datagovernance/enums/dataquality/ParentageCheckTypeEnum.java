package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 血缘检查类型
 * @date 2022/5/31 16:33
 */
public enum ParentageCheckTypeEnum implements BaseEnum {

    NONE(0, "空"),
    CHECK_UPSTREAM_BLOODLINE(1, "检查上游血缘是否断裂"),
    CHECK_DOWNSTREAM_BLOODLINE(2, "检查下游血缘是否断裂"),
    CHECK_UPSTREAM_AND_DOWNSTREAM_BLOODLINE(3, "检查上下游血缘是否断裂");

    ParentageCheckTypeEnum(int value, String name) {
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


    public static ParentageCheckTypeEnum getEnum(int value) {
        for (ParentageCheckTypeEnum e : ParentageCheckTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return ParentageCheckTypeEnum.NONE;
    }

    public static ParentageCheckTypeEnum getEnumByName(String name) {
        for (ParentageCheckTypeEnum e : ParentageCheckTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return ParentageCheckTypeEnum.NONE;
    }
}
