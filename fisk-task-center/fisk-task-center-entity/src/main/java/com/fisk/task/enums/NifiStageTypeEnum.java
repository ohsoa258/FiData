package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum NifiStageTypeEnum implements BaseEnum {

    QUERY_PHASE(5, "query_phase"),
    TRANSITION_PHASE(6, "transition_phase"),
    INSERT_PHASE(7, "insert_phase"),
    NOT_RUN(1, "未运行"),
    RUNNING(2, "正在运行"),
    SUCCESSFUL_RUNNING(3, "运行成功"),
    RUN_FAILED(4, "运行失败");


    NifiStageTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
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

    public static NifiStageTypeEnum getValue(String name) {
        NifiStageTypeEnum[] carTypeEnums = values();
        for (NifiStageTypeEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static NifiStageTypeEnum getName(int value) {
        NifiStageTypeEnum[] carTypeEnums = values();
        for (NifiStageTypeEnum carTypeEnum : carTypeEnums) {
            if (carTypeEnum.value == value) {
                return carTypeEnum;
            }
        }
        return null;
    }
}
