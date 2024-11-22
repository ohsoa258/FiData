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
    NULL_CHECK(101, "空值检查"),
    RANGE_CHECK(102, "值域检查"),
    STANDARD_CHECK(103, "规范检查"),
    DUPLICATE_DATA_CHECK(104, "重复数据检查"),
    FLUCTUATION_CHECK(105, "波动检查"),
    PARENTAGE_CHECK(106, "血缘检查"),
    REGEX_CHECK(107, "正则表达式检查"),
    SQL_SCRIPT_CHECK(108, "SQL脚本检查"),

    DATASET_CHECK(109, "数据集对比检查");

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
