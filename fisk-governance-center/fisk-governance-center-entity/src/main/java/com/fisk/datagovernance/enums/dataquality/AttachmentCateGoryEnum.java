package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 附件类型
 * @date 2022/5/31 16:33
 */
public enum AttachmentCateGoryEnum implements BaseEnum {
    NONE(0, "空"),
    QUALITY_VERIFICATION_SUMMARY_REPORT(100, "质量校验summary报告"),
    DATA_CLEANING_REPORT(200, "数据清洗报告"),
    INTELLIGENT_DISCOVERY_REPORT(300, "智能发现报告"),
    DATA_INSPECTION_LOG_REPORT(400, "数据检查日志报告"),
    DATA_OPERATION_AND_MAINTENANCE_GENERATE_IMPORT_TEMPLATES(500, "数据运维生成导入模板"),
    QUALITY_VERIFICATION_RULES_VERIFICATION_DETAIL_REPORT(600, "质量校验规则校验明细报告");

    AttachmentCateGoryEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

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


    public static AttachmentCateGoryEnum getEnum(int value) {
        for (AttachmentCateGoryEnum e : AttachmentCateGoryEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return AttachmentCateGoryEnum.NONE;
    }

    public static AttachmentCateGoryEnum getEnumByName(String name) {
        for (AttachmentCateGoryEnum e : AttachmentCateGoryEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return AttachmentCateGoryEnum.NONE;
    }
}
