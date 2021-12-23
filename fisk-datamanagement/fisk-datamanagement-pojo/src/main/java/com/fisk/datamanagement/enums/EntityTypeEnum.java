package com.fisk.datamanagement.enums;

import com.fisk.common.enums.BaseEnum;
import com.fisk.common.filter.dto.FilterEnum;

/**
 * @author JianWenYang
 */
public enum EntityTypeEnum implements BaseEnum {

    RDBMS_INSTANCE(1,"rdbms_instance"),
    RDBMS_DB(2,"rdbms_db"),
    RDBMS_TABLE(3,"rdbms_table"),
    RDBMS_COLUMN(4,"rdbms_column"),
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
            String queryName=carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return EntityTypeEnum.OTHER;
    }

}
