package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-04-07
 * @Description:
 */
public enum ApprovalNodeStateEnum implements BaseEnum {
    /**
     * 审批工单节点状态
     */
    APPROVE(1,"同意"),
    REFUSED(2,"拒绝"),
    IN_PROGRESS(3,"待审核");
    @EnumValue
    private final int value;
    @JsonValue
    private final String name;

    ApprovalNodeStateEnum(int value, String name) {
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
