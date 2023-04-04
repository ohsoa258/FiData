package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

public enum ProcessAssemblyTypeEnum implements BaseEnum {

    NONE(0, "空"),
    TRIGGER(1000, "触发器"),
    CONDITIONAL_EXPRESS(2000, "条件表达式"),
    SQL_SCRIPT(3000, "SQL脚本"),
    OPEN_API(4000, "SQL脚本"),
    FIELD_ASSIGNMENT(5000, "字段赋值");

    ProcessAssemblyTypeEnum(int value, String name) {
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

    public static ProcessAssemblyTypeEnum getEnum(int value) {
        for (ProcessAssemblyTypeEnum e : ProcessAssemblyTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return ProcessAssemblyTypeEnum.NONE;
    }
}
