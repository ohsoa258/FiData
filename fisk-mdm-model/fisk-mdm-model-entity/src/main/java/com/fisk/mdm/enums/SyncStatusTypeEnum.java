package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/4/5 12:11
 */
public enum SyncStatusTypeEnum implements BaseEnum {

    /**
     * stg表同步数据状态
     */
    UPLOADED_SUCCESSFULLY(0, "上传成功"),

    SUBMITTED_SUCCESSFULLY(1, "提交成功"),

    SUBMISSION_FAILED(2, "提交失败"),

    UPLOADED_FAILED(3, "导入失败");

    SyncStatusTypeEnum(int value, String name) {
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
