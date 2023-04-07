package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
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
    IN_PROGRESS(1,"进行中"),
    APPROVE(2,"已同意"),
    REFUSED(3,"已拒绝");
    @EnumValue
    private final int value;
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
