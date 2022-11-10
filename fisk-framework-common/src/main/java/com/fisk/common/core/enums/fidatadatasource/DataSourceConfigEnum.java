package com.fisk.common.core.enums.fidatadatasource;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum DataSourceConfigEnum implements BaseEnum {

    DMP_DW(1, "dmp_dw"),
    DMP_ODS(2, "dmp_ods"),
    DMP_MDM(3, "dmp_mdm"),
    DMP_OLAP(4, "dmp_olap"),
    NONE(5, "none");

    private final String name;
    private final int value;

    DataSourceConfigEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static DataSourceConfigEnum getEnum(int value) {
        for (DataSourceConfigEnum e : DataSourceConfigEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return DataSourceConfigEnum.NONE;
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
