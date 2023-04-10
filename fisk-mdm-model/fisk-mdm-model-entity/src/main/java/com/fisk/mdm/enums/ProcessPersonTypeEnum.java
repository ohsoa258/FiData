package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-03-31
 * @Description: 流程节点审批人类型
 */
public enum ProcessPersonTypeEnum implements BaseEnum {
    /**
     * 流程节点审批人类型
     */
    PERSON_TYPE1_ENUM(1,"角色"),

    PERSON_TYPE2_ENUM(2,"用户");
    @EnumValue
    private final int value;
    private final String name;

    ProcessPersonTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
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
