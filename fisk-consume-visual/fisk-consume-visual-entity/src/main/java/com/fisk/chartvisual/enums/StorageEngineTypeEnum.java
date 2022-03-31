package com.fisk.chartvisual.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/1/6 16:11
 */
public enum StorageEngineTypeEnum implements BaseEnum {
    DMP(1,"白泽数据源"),
    MDX(2,"CUBE"),
    VIEW(3,"视图");

    StorageEngineTypeEnum(int value, String name) {
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
