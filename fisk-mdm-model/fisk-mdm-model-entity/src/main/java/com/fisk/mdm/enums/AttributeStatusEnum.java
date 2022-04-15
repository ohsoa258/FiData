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
    INSERT(0,"待新增"),
    UPDATE(1,"待修改"),
    SUBMITTED(2,"已提交");


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
