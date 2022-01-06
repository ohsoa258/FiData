package com.fisk.dataaccess.enums;

/**
 * @author Lock
 */
public enum SystemVariableTypeEnum {
    /**
     * 查询类型
     */
    START_TIME("incremental_objectivescore_start","上次同步开始时间"),
    END_TIME("incremental_objectivescore_end","上次同步结束时间");

    SystemVariableTypeEnum(String value, String name) {
        this.name = name;
        this.value = value;
    }

    private final String value;
    private final String name;

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
