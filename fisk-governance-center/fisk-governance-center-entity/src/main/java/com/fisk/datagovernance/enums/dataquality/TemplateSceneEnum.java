package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 模板应用场景
 * @date 2022/5/16 14:00
 */
public enum TemplateSceneEnum implements BaseEnum {
    NONE(0, "空"),
    DATACHECK_WEBCHECK(100, "页面校验"),
    DATACHECK_SYNCCHECK(101, "同步校验"),
    DATACHECK_QUALITYREPORT(102, "校验报告"),
    BUSINESSFILTER_SYNCFILTER(200, "同步清洗"),
    BUSINESSFILTER_FILTERREPORT(201, "清洗报告"),
    LIFECYCLE_REPORT(300, "生命周期报告"),
    NOTICE_DATACHECK(400, "数据校验告警"),
    NOTICE_BUSINESSFILTER(401, "业务清洗告警"),
    NOTICE_LIFECYCLE(402, "生命周期告警");

    TemplateSceneEnum(int value, String name) {
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

    public static TemplateSceneEnum  getEnum(int value){
        for (TemplateSceneEnum e:TemplateSceneEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return TemplateSceneEnum.NONE;
    }
}
