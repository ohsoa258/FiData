package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 模块类型
 * @date 2022/3/22 13:57
 */
public enum ModuleTypeEnum implements BaseEnum {
    NONE(0, "空"),
    DATACHECK_MODULE(100, "数据校验"),
    BIZCHECK_MODULE(200, "业务清洗"),
    LIFECYCLE_MODULE(300, "生命周期"),
    WARNNOTICE_MODULE(400, "告警通知");

    ModuleTypeEnum(int value, String name) {
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

    public static ModuleTypeEnum  getEnum(int value){
        for (ModuleTypeEnum e:ModuleTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return ModuleTypeEnum.NONE;
    }
}
