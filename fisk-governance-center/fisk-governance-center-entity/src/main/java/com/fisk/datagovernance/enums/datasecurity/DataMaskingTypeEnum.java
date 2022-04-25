package com.fisk.datagovernance.enums.datasecurity;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author Lock
 * @version 1.0
 * @description
 * @date 2022/3/22 14:01
 */
public enum DataMaskingTypeEnum implements BaseEnum {
    /**
     * 校验规则类型
     */
    NONE(-1, "空"),
    KEEP_FIELD(0, "保留字段前几位"),
    VALUE_ENCRYPT(1, "值加密");

    DataMaskingTypeEnum(int value, String name) {
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

    public static DataMaskingTypeEnum getEnum(int value) {
        for (DataMaskingTypeEnum e : DataMaskingTypeEnum.values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return DataMaskingTypeEnum.NONE;
    }
}
