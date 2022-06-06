package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 数据源类型
 * @date 2022/5/16 13:09
 */
public enum SourceTypeEnum implements BaseEnum {

    NONE(0, "空"),
    FiData(1, "FiData"),
    custom(2, "自定义");

    SourceTypeEnum(int value, String name) {
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

    public static SourceTypeEnum getEnum(int value) {
        for (SourceTypeEnum e : SourceTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return SourceTypeEnum.NONE;
    }
}
