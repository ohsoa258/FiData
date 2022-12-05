package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum AppServiceTypeEnum implements BaseEnum {

    /**
     * api类型
     */
    API(1, "API"),
    TABLE(2, "表"),
    FILE(3, "文件");

    private final int value;
    private final String name;

    AppServiceTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}
