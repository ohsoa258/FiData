package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

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
    NONE(0, "空模板", ""),
    FIELD_STRONG_RULE_TEMPLATE(100, "字段强规则模板", "task.build.governance.fieldStrongRule.template.flow"),
    FIELD_AGGREGATE_THRESHOLD_TEMPLATE(101, "字段聚合波动阈值模板", "task.build.governance.fieldAggregateThreshold.template.flow"),
    ROWCOUNT_THRESHOLD_TEMPLATE(102, "表行数波动阈值模板", "task.build.governance.rowCountThreshold.template.flow"),
    EMPTY_TABLE_CHECK_TEMPLATE(103, "空表校验模板", "task.build.governance.emptyTableCheck.template.flow"),
    UPDATE_TABLE_CHECK_TEMPLATE(104, "表更新校验模板", "task.build.governance.updateTableCheck.template.flow"),
    TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE(105, "表血缘断裂校验模板", "task.build.governance.tableBloodKinshipCheck.template.flow"),
    BUSINESS_CHECK_TEMPLATE(106, "业务验证模板", "task.build.governance.businessCheck.template.flow"),
    SIMILARITY_TEMPLATE(107, "相似度模板", "task.build.governance.similarity.template.flow"),
    BUSINESS_FILTER_TEMPLATE(200, "业务清洗模板", "task.build.governance.businessFilter.template.flow"),
    SPECIFY_TIME_RECYCLING_TEMPLATE(300, "指定时间回收模板", "task.build.governance.specifyTimeRecycling.template.flow"),
    EMPTY_TABLE_RECOVERY_TEMPLATE(301, "空表回收模板", "task.build.governance.emptyTableRecovery.template.flow"),
    NO_REFRESH_DATA_RECOVERY_TEMPLATE(302, "数据无刷新回收模板", "task.build.governance.noRefreshDataRecovery.template.flow"),
    DATA_BLOOD_KINSHIP_RECOVERY_TEMPLATE(303, "数据血缘断裂回收模板", "task.build.governance.dataBloodKinshipRecovery.template.flow"),
    EMAIL_NOTICE_TEMPLATE(400, "邮件通知模板", ""),
    SYSTEM_NOTICE_TEMPLATE(401, "站内消息模板", "");

    TemplateTypeEnum(int value, String name, String topic) {
        this.name = name;
        this.value = value;
        this.topic = topic;
    }

    private final int value;
    private final String name;
    private final String topic;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public static TemplateTypeEnum getEnum(int value) {
        for (TemplateTypeEnum e : TemplateTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return TemplateTypeEnum.NONE;
    }
}
