package com.fisk.task.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author cfk
 */
public enum BusinessOperatorEnum  implements BaseEnum {


    //1:大于  2:小于  3:等于  4:大于等于  5:小于等于
    GREATER_THAN(1,">"),
    LESS_THAN(2,"<"),
    EQUAL_TO(3,"="),
    GREAT_THAN_OR_EQUAL_TO(4,">="),
    LESS_THAN_OR_EQUAL_TO(5,"<=");

    BusinessOperatorEnum(int value, String name) {
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
