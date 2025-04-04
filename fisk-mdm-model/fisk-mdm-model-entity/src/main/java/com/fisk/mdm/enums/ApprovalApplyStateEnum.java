package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-04-06
 * @Description:
 */
public enum ApprovalApplyStateEnum implements BaseEnum {
    /**
     * 审批工单状态
     */
    NOT_APPROVAL(0,"待审核"),
    APPROVE(1,"已同意"),
    REFUSED(2,"已拒绝"),
    IN_PROGRESS(3,"进行中"),

    ROLL_BACK(4,"已撤回");

    @EnumValue
    private final int value;
    @JsonValue
    private final String name;

    ApprovalApplyStateEnum(int value, String name) {
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
