package com.fisk.datamodel.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum RelateTableTypeEnum implements BaseEnum {

    /**
     * 内连接
     */
    INNER_JOIN(1,"inner join"),
    LEFT_JOIN(2,"left join"),
    RIGHT_JOIN(3,"right join"),
    FULL_JOIN(4,"full join"),
    CROSS_JOIN(5,"cross join"),
    OTHER(-1,"其他");

    RelateTableTypeEnum(int value, String name) {
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

    public static RelateTableTypeEnum getValue(String value) {
        RelateTableTypeEnum[] carTypeEnums = values();
        for (RelateTableTypeEnum carTypeEnum : carTypeEnums) {
            String queryValue=carTypeEnum.getName();
            if (queryValue.equals(value)) {
                return carTypeEnum;
            }
        }
        return RelateTableTypeEnum.OTHER;
    }

}
