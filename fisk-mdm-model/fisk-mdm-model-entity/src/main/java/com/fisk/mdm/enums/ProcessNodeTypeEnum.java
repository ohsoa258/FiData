package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-03-31
 * @Description: 流程节点类型
 */
public enum ProcessNodeTypeEnum implements BaseEnum {
    /**
     * 流程节点类型
     */
    NODE_TYPE1_ENUM(1,"申请人"),

    NODE_TYPE2_ENUM(2,"审批人");
    @EnumValue
    private final int value;
    @JsonValue
    private final String name;

    ProcessNodeTypeEnum(int value, String name) {
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
