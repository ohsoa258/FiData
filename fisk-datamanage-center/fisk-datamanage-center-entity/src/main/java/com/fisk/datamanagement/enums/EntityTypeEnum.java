package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum EntityTypeEnum implements BaseEnum {
    /**
     * 实例
     */
    RDBMS_INSTANCE(1,"rdbms_instance"),
    RDBMS_DB(2,"rdbms_db"),
    RDBMS_TABLE(3,"rdbms_table"),
    REPORT(4,"report"),
    WEB_API(5,"web_api"),
    RDBMS_COLUMN(6,"rdbms_column"),
    PROCESS(7,"Process"),
    DATASET_PROCESS_INPUTS(8,"dataset_process_inputs"),
    DELETED(9,"DELETED"),
    ACTIVE(10,"ACTIVE"),
    OTHER(-1,"other");

    EntityTypeEnum(int value, String name) {
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

    public static EntityTypeEnum getValue(String name) {
        EntityTypeEnum[] carTypeEnums = values();
        for (EntityTypeEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return EntityTypeEnum.OTHER;
    }

    public static EntityTypeEnum getValue(Integer value) {
        EntityTypeEnum[] carTypeEnums = values();
        for (EntityTypeEnum carTypeEnum : carTypeEnums) {
            Integer queryName = carTypeEnum.value;
            if (queryName.equals(value)) {
                return carTypeEnum;
            }
        }
        return EntityTypeEnum.OTHER;
    }

}
