package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 模板类型
 * @date 2022/3/22 13:58
 */
public enum TemplateTypeEnum implements BaseEnum {
    /**
     * 模板类型
     */
    FIELD_STRONG_RULE_TEMPLATE(100, "字段强规则模板"),
    FIELD_AGGREGETION_TEMPLATE(101, "字段聚合波动阀值模板"),
    TABLEROW_THRESHOLD_TEMPLATE(102, "表行数波动阀值模板"),
    EMPTY_TABLE_CHECK_TEMPLATE(103, "空表校验模板"),
    UPDATE_TABLE_CHECK_TEMPLATE(104, "表更新校验模板"),
    TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE(105, "表血缘断裂校验模板"),
    BUSINESS_CHECK_TEMPLATE(106, "业务验证模板"),
    BUSINESS_CLEAN_TEMPLATE(200, "业务清洗模板"),
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
}
