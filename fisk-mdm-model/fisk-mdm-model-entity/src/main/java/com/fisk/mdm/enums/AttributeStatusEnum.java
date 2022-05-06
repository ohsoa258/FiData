package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author ChenYa
 */
public enum AttributeStatusEnum  implements BaseEnum {

    /**
     * 属性状态
     */
    INSERT(0,"新增待发布"),
    UPDATE(1,"修改待发布"),
    SUBMITTED(2,"发布"),
    DELETE(3,"删除待发布");


    @EnumValue
    private final int value;
    private final String name;


    AttributeStatusEnum(int value, String name) {
        this.value=value;
        this.name=name;
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
