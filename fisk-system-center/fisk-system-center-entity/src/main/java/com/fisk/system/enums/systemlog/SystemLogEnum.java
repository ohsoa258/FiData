package com.fisk.system.enums.systemlog;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author lishiji
 */
public enum SystemLogEnum implements BaseEnum {

    /**
     * 服务名称以及服务名称对应的日志名称
     */
    NONE(-1, "空", "空", "空"),
    FISK_API_SERVEICE(0, "fisk-api-serveice", "./logs/fisk-api-serveice/fisk-api-serveice.log", "./logs/fisk-api-serveice/"),
    FISK_CONSUME_SERVEICE(1, "fisk-consume-serveice", "./logs/fisk-consume-serveice/fisk-consume-serveice.log", "./logs/fisk-consume-serveice/"),
    FISK_CONSUME_VISUAL(2, "fisk-consume-visual", "./logs/consumeVisual/chartVisual.log", "./logs/consumeVisual/"),
    FISK_DATAMANAGE_CENTER(3, "fisk-datamanage-center", "./logs/datamanagement/datamanagement.log", "./logs/datamanagement/"),
    FISK_FACTORY_ACCESS(4, "fisk-factory-access", "./logs/factoryAccess/factoryAccess.log", "./logs/factoryAccess/"),
    FISK_FACTORY_DISPATCH(5, "fisk-factory-dispatch", "./logs/dispatch/factoryDispatch.log", "./logs/dispatch/"),
    FISK_FACTORY_MODEL(6, "fisk-factory-model", "./logs/datamodel/datamodel.log", "./logs/datamodel/"),
    FISK_FRAMEWORK_AUTHORIZATION(7, "fisk-framework-authorization", "./logs/auth/auth.log", "./logs/auth/"),
    FISK_FRAMEWORK_GATEWAY(8, "fisk-framework-gateway", "./logs/gateway/gateway.log", "./logs/gateway/"),
    FISK_FRAMEWORK_REGISTRY(9, "fisk-framework-registry", "./logs/registry/registry.log", "./logs/registry/"),
    FISK_GOVERNANCE_CENTER(10, "fisk-governance-center", "./logs/governance/governance.log", "./logs/governance/"),
    FISK_LICENSE_REGISTRY(11, "fisk-license-registry", "./logs/fisk-license-registry/fisk-license-registry", "./logs/fisk-license-registry/"),
    FISK_MDM_MODEL(12, "fisk-mdm-model", "./logs/mdm/mdmModel.log", "./logs/mdm"),
    FISK_SYSTEM_CENTER(13, "fisk-system-center", "./logs/systemCenter/system.log", "./logs/systemCenter/"),
    FISK_TASK_CENTER(14, "fisk-task-center", "./logs/task/task.log", "./logs/task/");


    SystemLogEnum(int value, String name, String logName, String logHome) {
        this.value = value;
        this.name = name;
        this.logName = logName;
        this.logHome = logHome;
    }

    /**
     * 对应数值
     */
    private final int value;

    /**
     * 服务名称
     */
    private final String name;

    /**
     * 当天日志名称
     */
    private final String logName;

    /**
     * 当前服务的日志包路径
     */
    private final String logHome;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getLogName() {
        return logName;
    }

    public String getLogHome() {
        return logHome;
    }

    public static SystemLogEnum getEnum(int value) {
        for (SystemLogEnum e : SystemLogEnum.values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return SystemLogEnum.NONE;
    }

}
