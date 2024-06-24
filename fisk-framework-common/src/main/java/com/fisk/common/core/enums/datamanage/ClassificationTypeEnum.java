package com.fisk.common.core.enums.datamanage;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JinXingWang
 */

public enum ClassificationTypeEnum implements BaseEnum {
    ALL(0,"全部","全部"),
    DATA_ACCESS(1, "数据接入", "数据贴源，对接上游系统数据"),
    ANALYZE_DATA(2, "数仓建模", "数仓建模，数据仓库数据"),
    API_GATEWAY_SERVICE(3, "API网关服务", "通过API，向下游消费数据"),
    DATA_DISTRIBUTION(4, "数据库分发服务", "通过数据库和接口配置，实现数据交互到下游数据库"),
    VIEW_ANALYZE_SERVICE(5, "数据分析试图服务", "提供数据查询视图"),
    MASTER_DATA(6, "主数据", "主数据"),
    EXTERNAL_DATA(7,"外部数据源","外部数据源")

    ;

    private final int value;
    private final String name;
    private final String description;

    ClassificationTypeEnum(int value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static ClassificationTypeEnum getEnumByName(String name) {
        ClassificationTypeEnum[] carTypeEnums = values();
        for (ClassificationTypeEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static ClassificationTypeEnum getEnumByValue(int value) {
        ClassificationTypeEnum[] carTypeEnums = values();
        for (ClassificationTypeEnum carTypeEnum : carTypeEnums) {
            if (carTypeEnum.value == value) {
                return carTypeEnum;
            }
        }
        return null;
    }
}


