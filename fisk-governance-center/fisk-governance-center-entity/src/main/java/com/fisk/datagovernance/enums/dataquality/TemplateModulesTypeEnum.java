package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 模板模块
 * @date 2022/3/22 13:57
 */
public enum TemplateModulesTypeEnum implements BaseEnum {
    /**
     * 模板模块
     */
    NONE(0, "空模板"),
    DATACHECK_MODULE(100, "数据校验"),
    BIZCHECK_MODULE(200, "业务清洗"),
    LIFECYCLE_MODULE(300, "生命周期"),
    WARNNOTICE_MODULE(400, "告警通知");

    TemplateModulesTypeEnum(int value, String name) {
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

    public static TemplateModulesTypeEnum  getEnum(int value){
        for (TemplateModulesTypeEnum e:TemplateModulesTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return TemplateModulesTypeEnum.NONE;
    }
}
