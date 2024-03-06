package com.fisk.common.core.enums.system;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 系统数据源业务类型
 * @date 2022/10/27 14:14
 */
public enum AuditServiceTypeEnum implements BaseEnum {

    NONE(0, "none"),
    FISK_API_SERVICE(1, ""),
    FISK_CONSUME_SERVICE(2, "数据消费"),
    FISK_CONSUME_VISUAL(3, ""),
    FISK_DATAMANAGE_CENTER(4, "数据资产"),
    FISK_FACTORY_ACCESS(5, "数据接入"),
    FISK_FACTORY_DISPATCH(6, "数据管道"),
    FISK_FACTORY_MODEL(7, "数仓建模"),
    FISK_FRAMEWORK_AUTHORIZATION(8, "身份认证"),
    FISK_FRAMEWORK_GATEWAY(9, "网关服务"),
    FISK_FRAMEWORK_REGISTRY(10, "注册中心"),
    FISK_GOVERNANCE_CENTER(11, "数据治理"),
    FISK_LICENSE_REGISTRY(12, ""),
    FISK_MDM_MODEL(13, "主数据"),
    FISK_SYSTEM_CENTER(14, "系统管理"),
    FISK_TASK_CENTER(15, "任务中心");

    AuditServiceTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final String name;
    private final int value;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static AuditServiceTypeEnum getEnum(int value) {
        for (AuditServiceTypeEnum e : AuditServiceTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return AuditServiceTypeEnum.NONE;
    }
}
