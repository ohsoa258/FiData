package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum ProcessTypeEnum implements BaseEnum {

    /**
     * 实例
     */
    SQL_PROCESS(1, "sql_process"),
    DIMENSION_PROCESS(2, "dimension_process"),
    CUSTOM_SCRIPT_PROCESS(3, "custom_script_process"),
    TEMP_TABLE_PROCESS(4, "temp_table_process");

    private final int value;
    private final String name;

    ProcessTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}
