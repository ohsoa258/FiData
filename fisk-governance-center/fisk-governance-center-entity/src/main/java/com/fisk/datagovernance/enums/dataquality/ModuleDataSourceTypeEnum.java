package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 组件数据源类型
 * @date 2022/3/22 13:59
 */
public enum ModuleDataSourceTypeEnum implements BaseEnum {
    /**
     * 组件数据源类型
     */
    NONE(0, "空数据源"),
    DATAQUALITY(1, "数据质量"),
    METADATA(2, "元数据");

    ModuleDataSourceTypeEnum(int value, String name) {
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

    public static ModuleDataSourceTypeEnum getEnum(int value) {
        for (ModuleDataSourceTypeEnum e : ModuleDataSourceTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return ModuleDataSourceTypeEnum.NONE;
    }
}
