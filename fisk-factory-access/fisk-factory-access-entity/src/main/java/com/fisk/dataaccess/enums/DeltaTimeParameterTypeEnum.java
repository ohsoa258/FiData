package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author cfk
 */
public enum DeltaTimeParameterTypeEnum implements BaseEnum {
    /*
     *增量时间参数类型
     */
    CONSTANT(1,"常量"),
    VARIABLE(2,"变量"),
    THE_DEFAULT_EMPTY(3,"默认值");

    DeltaTimeParameterTypeEnum(int value, String name) {
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

    public static DeltaTimeParameterTypeEnum getName(String name) {
        DeltaTimeParameterTypeEnum[] enums = values();
        for (DeltaTimeParameterTypeEnum typeEnum : enums) {
            String queryName = typeEnum.name();
            if (queryName.equals(name)) {
                return typeEnum;
            }
        }
        return null;
    }

}
