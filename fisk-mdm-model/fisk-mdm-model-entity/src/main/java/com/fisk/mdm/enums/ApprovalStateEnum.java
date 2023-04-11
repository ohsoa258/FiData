package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 审批启用状态
 */
public enum ApprovalStateEnum implements BaseEnum {
    /**
     * 审批状态
     */
    CLOSE(0,"关闭"),
    OPEN(1,"开启");
    @EnumValue
    private final int value;
    @JsonValue
    private final String name;

    ApprovalStateEnum(int value, String name) {
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
