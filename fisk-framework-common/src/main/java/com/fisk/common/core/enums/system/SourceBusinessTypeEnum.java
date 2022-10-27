package com.fisk.common.core.enums.system;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 系统数据源业务类型
 * @date 2022/10/27 14:14
 */
public enum SourceBusinessTypeEnum implements BaseEnum {
    NONE(0, "NONE"),
    DW(1, "DW"),
    ODS(2, "ODS"),
    MDM(3, "MDM"),
    OLAP(4, "OLAP");

    SourceBusinessTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final String name;
    private final int value;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static SourceBusinessTypeEnum getEnum(int value) {
        for (SourceBusinessTypeEnum e : SourceBusinessTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return SourceBusinessTypeEnum.NONE;
    }
}
