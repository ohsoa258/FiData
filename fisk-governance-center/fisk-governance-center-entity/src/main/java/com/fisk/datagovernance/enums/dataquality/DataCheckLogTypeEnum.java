package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 数据检查日志类型
 * @date 2022/5/31 16:33
 */
public enum DataCheckLogTypeEnum implements BaseEnum {

    NONE(0, "空"),
    INTERFACE_DATA_CHECK_LOG(1, "接口同步数据校验日志（同步前）"),
    NIFI_SYNCHRONIZATION_DATA_CHECK_LOG(2, "NIFI同步数据校验日志（同步中）"),
    SUBSCRIPTION_REPORT_RULE_CHECK_LOG(3, "订阅报告规则校验日志（同步后）");

    DataCheckLogTypeEnum(int value, String name) {
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


    public static DataCheckLogTypeEnum getEnum(int value) {
        for (DataCheckLogTypeEnum e : DataCheckLogTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return DataCheckLogTypeEnum.NONE;
    }

    public static DataCheckLogTypeEnum getEnumByName(String name) {
        for (DataCheckLogTypeEnum e : DataCheckLogTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return DataCheckLogTypeEnum.NONE;
    }
}
