package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/4/5 12:11
 */
public enum EventTypeEnum implements BaseEnum {

    /**
     * 事件类型
     */
    SAVE(0, "新增"),

    UPDATE(1, "修改"),

    DELETE(2, "删除"),
    ROLLBACK(3, "回滚"),
    IMPORT(4, "导入");

    EventTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @EnumValue
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
