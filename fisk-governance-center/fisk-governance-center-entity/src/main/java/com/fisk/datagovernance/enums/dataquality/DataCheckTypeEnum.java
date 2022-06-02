package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验类型
 * @date 2022/5/31 16:33
 */
public enum DataCheckTypeEnum implements BaseEnum {

    /**
     * 数据校验类型
     */
    NONE(0,"空"),
    TEXTLENGTH_CHECK(1, "文本长度校验"),
    DATEFORMAT_CHECK(2, "日期格式校验"),
    SEQUENCERANGE_CHECK(3, "序列范围校验");

    DataCheckTypeEnum(int value, String name) {
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

    public static DataCheckTypeEnum getEnum(int value){
        for (DataCheckTypeEnum e: DataCheckTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return DataCheckTypeEnum.NONE;
    }

    public static DataCheckTypeEnum getEnumByName(String name){
        for (DataCheckTypeEnum e: DataCheckTypeEnum.values()) {
            if(e.getName().equals(name))
                return e;
        }
        return DataCheckTypeEnum.NONE;
    }
}
