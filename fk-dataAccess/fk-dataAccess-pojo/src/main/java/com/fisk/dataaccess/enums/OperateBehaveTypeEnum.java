package com.fisk.dataaccess.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 * @version 1.0
 * @description 操作表行为枚举类
 * @date 2022/1/10 17:14
 */
public enum OperateBehaveTypeEnum implements BaseEnum {
    /**
     * 行为类型
     */
    UPDATE_APP(1, "修改应用"),
    DELETE_APP(2,"删除应用"),
    UPDATE_TABLE(3, "修改表"),
    DELETE_TABLE(4, "删除表");

    OperateBehaveTypeEnum(int value, String name) {
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
