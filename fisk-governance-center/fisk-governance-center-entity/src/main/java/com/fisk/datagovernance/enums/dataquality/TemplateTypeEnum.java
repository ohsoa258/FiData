package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 模板类型
 * @date 2022/3/22 13:58
 */
public enum TemplateTypeEnum implements BaseEnum {
    NONE(0, "空模板", ""),
    FIELD_RULE_TEMPLATE(100, "字段规则模板", "FIELD"),
    FIELD_AGGREGATE_TEMPLATE(101, "字段聚合波动阈值模板", "FIELD"),
    TABLECOUNT_TEMPLATE(102, "表行数波动阈值模板", "TABLE"),
    EMPTY_TABLE_CHECK_TEMPLATE(103, "空表校验模板", "TABLE"),
    UPDATE_TABLE_CHECK_TEMPLATE(104, "表更新校验模板", "TABLE"),
    TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE(105, "表血缘断裂校验模板", "TABLE"),
    BUSINESS_CHECK_TEMPLATE(106, "业务验证模板", "TABLE"),
    SIMILARITY_TEMPLATE(107, "相似度模板", "FIELD"),
    DATA_MISSING_TEMPLATE(108, "数据缺失模板", "FIELD"),
    BUSINESS_FILTER_TEMPLATE(200, "业务清洗模板", "TABLE"),
    SPECIFY_TIME_RECYCLING_TEMPLATE(300, "指定时间回收模板", "TABLE"),
    EMPTY_TABLE_RECOVERY_TEMPLATE(301, "空表回收模板", "TABLE"),
    NO_REFRESH_DATA_RECOVERY_TEMPLATE(302, "数据无刷新回收模板", "TABLE"),
    DATA_BLOOD_KINSHIP_RECOVERY_TEMPLATE(303, "数据血缘断裂回收模板", "TABLE"),
    EMAIL_NOTICE_TEMPLATE(400, "邮件通知模板", ""),
    SYSTEM_NOTICE_TEMPLATE(401, "站内消息模板", "");

    TemplateTypeEnum(int value, String name, String rangeType) {
        this.name = name;
        this.value = value;
        this.rangeType = rangeType;
    }

    private final int value;
    private final String name;
    private final String rangeType;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getRangeType() {
        return rangeType;
    }

    public static TemplateTypeEnum getEnum(int value) {
        for (TemplateTypeEnum e : TemplateTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return TemplateTypeEnum.NONE;
    }
}
