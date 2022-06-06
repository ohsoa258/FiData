package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/4/5 12:11
 */
public enum MdmStatusTypeEnum implements BaseEnum {

    /**
     * mdm的状态
     */
    NOT_CREATED(0,"未创建"),

    CREATED_SUCCESSFULLY(1,"创建成功"),

    CREATED_FAIL(2,"创建失败");

    MdmStatusTypeEnum(int value, String name) {
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
