package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 模板类型
 * @date 2022/3/22 13:58
 */
public enum TemplateTypeEnum implements BaseEnum {
    NONE(0, "空模板"),
    FIELD_RULE_TEMPLATE(100, "字段规则模板"),
    FIELD_AGGREGATE_TEMPLATE(101, "字段聚合波动阈值模板"),
    TABLECOUNT_TEMPLATE(102, "表行数波动阈值模板"),
    EMPTY_TABLE_CHECK_TEMPLATE(103, "空表校验模板"),
    UPDATE_TABLE_CHECK_TEMPLATE(104, "表更新校验模板"),
    TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE(105, "表血缘断裂校验模板"),
    BUSINESS_CHECK_TEMPLATE(106, "业务验证模板"),
    SIMILARITY_TEMPLATE(107, "相似度模板"),
    DATA_MISSING_TEMPLATE(108, "数据缺失模板"),
    API_FILTER_TEMPLATE(200, "API清洗模板"),
    SYNC_FILTER_TEMPLATE(201, "同步清洗模板"),
    FILTER_REPORT_TEMPLATE(202, "清洗报告模板"),
    SPECIFY_TIME_RECYCLING_TEMPLATE(300, "指定时间回收模板"),
    EMPTY_TABLE_RECOVERY_TEMPLATE(301, "空表回收模板"),
    NO_REFRESH_DATA_RECOVERY_TEMPLATE(302, "数据无刷新回收模板"),
    DATA_BLOOD_KINSHIP_RECOVERY_TEMPLATE(303, "数据血缘断裂回收模板"),
    EMAIL_NOTICE_TEMPLATE(400, "邮件通知模板"),
    SYSTEM_NOTICE_TEMPLATE(401, "站内消息模板");

    TemplateTypeEnum(int value, String name) {
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

    public static TemplateTypeEnum getEnum(int value) {
        for (TemplateTypeEnum e : TemplateTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return TemplateTypeEnum.NONE;
    }
}
