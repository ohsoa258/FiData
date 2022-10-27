package com.fisk.dataaccess.enums;

/**
 * @author Lock
 */
public enum SystemVariableTypeEnum {
    /**
     * 查询类型
     */
    START_TIME("@start_time","incremental_objectivescore_start"),
    STARTTIME("开始时间","'\\${incremental_objectivescore_start}'"),
    END_TIME("@end_time","incremental_objectivescore_end"),
    ENDTIME("结束时间","'\\${incremental_objectivescore_end}'"),
    QUERY_SQL("查询语句","query_sql"),
    HISTORICAL_TIME("历史时间","historical_time");

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

    public static SystemVariableTypeEnum getName(String name) {
        SystemVariableTypeEnum[] enums = values();
        for (SystemVariableTypeEnum typeEnum : enums) {
            String queryName = typeEnum.name();
            if (queryName.equals(name)) {
                return typeEnum;
            }
        }
        return null;
    }
}
