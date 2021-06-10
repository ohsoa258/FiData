package com.fisk.common.enums.chartvisual;

import com.fisk.common.enums.BaseEnum;

/**
 * 图表交互类型
 *
 * @author gy
 */

public enum InteractiveTypeEnum implements BaseEnum {

    /**
     * 图表交互类型（下钻，联动）
     */
    DRILL(0, "下钻"),
    LINKAGE(1, "钻取"),
    DEFAULT(99, "默认");

    InteractiveTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
