package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 审批异常处理
 */
public enum ExceptionApprovalEnum implements BaseEnum {
    /**
     * 审批节点内所有用户失效等情况的处理方式
     */
    EXCEPTION_APPROVAL_ENUM(1,"自动同意");
    @EnumValue
    private final int value;
    private final String name;

    ExceptionApprovalEnum(int value, String name) {
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
