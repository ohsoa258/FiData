package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;
import com.fisk.common.filter.dto.FilterEnum;

/**
 * @author JianWenYang
 */
public enum DataFactoryEnum implements BaseEnum {

    NUMBER_DIMENSION(4,"数仓维度"),
    NUMBER_FACT(5,"数仓事实"),
    ANALYSIS_DIMENSION(6,"分析维度"),
    ANALYSIS_FACT(7,"分析事实"),
    OTHER(-1,"其他");

    DataFactoryEnum(int value, String name) {
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

    public static DataFactoryEnum getValue(int value) {
        DataFactoryEnum[] carTypeEnums = values();
        for (DataFactoryEnum carTypeEnum : carTypeEnums) {
            int queryValue=carTypeEnum.value;
            if (queryValue==value) {
                return carTypeEnum;
            }
        }
        return DataFactoryEnum.OTHER;
    }

}
