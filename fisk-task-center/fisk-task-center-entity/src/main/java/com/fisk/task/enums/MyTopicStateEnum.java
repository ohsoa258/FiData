package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum MyTopicStateEnum implements BaseEnum {
    NOT_RUNNING(0, "未运行"),
    RUNNING(1, "正在运行");


    MyTopicStateEnum(int value, String name) {
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

    public static MyTopicStateEnum getValue(String name) {
        MyTopicStateEnum[] carTypeEnums = values();
        for (MyTopicStateEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static MyTopicStateEnum getName(int value) {
        MyTopicStateEnum[] carTypeEnums = values();
        for (MyTopicStateEnum carTypeEnum : carTypeEnums) {
            if (carTypeEnum.value == value) {
                return carTypeEnum;
            }
        }
        return null;
    }
}
