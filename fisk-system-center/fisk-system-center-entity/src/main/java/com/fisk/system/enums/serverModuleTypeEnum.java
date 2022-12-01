package com.fisk.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2021/11/4 14:03
 */
public enum serverModuleTypeEnum implements BaseEnum {

    /**
     * 数据接入
     */
    DATA_INPUT("数据接入", 0),
    /**
     * 数据建模
     */
    DATA_MODEL("数据建模", 1),
    /**
     * 数据调度
     */
    DATA_DISPATCH("数据调度", 2),
    /**
     * Api服务
     */
    DATA_SERVICE("Api服务", 3),
    /**
     * 数据治理
     */
    DATA_MANAGEMENT("数据治理", 4),
    /**
     * 平台数据源
     */
    PLATFORM_DATASOURCE("平台数据源", 5),
    /**
     * 数据治理_质量报告
     */
    DATA_GOVERNANCE_QUALITY_REPORT("质量报告", 6);

    private final String name;
    @EnumValue
    private final int value;

    serverModuleTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
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
