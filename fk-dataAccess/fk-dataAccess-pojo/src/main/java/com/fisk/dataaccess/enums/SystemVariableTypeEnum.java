package com.fisk.dataaccess.enums;

/**
 * @author Lock
 */
public enum SystemVariableTypeEnum {
    /**
     * 查询类型
     */
    START_TIME("@start_time","incremental_objectivescore_start"),
    END_TIME("@end_time","incremental_objectivescore_end");

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
