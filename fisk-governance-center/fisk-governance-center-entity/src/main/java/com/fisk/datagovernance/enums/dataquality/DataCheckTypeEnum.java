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
    NONE(0, "空", 0),
    TEXTLENGTH_CHECK(1, "文本长度校验", 3),
    DATEFORMAT_CHECK(2, "日期格式校验", 3),
    SEQUENCERANGE_CHECK(3, "序列范围校验", 3);

    DataCheckTypeEnum(int value, String name, int parentId) {
        this.name = name;
        this.value = value;
        this.parentId = parentId;
    }

    private final int value;
    private final String name;
    private final int parentId;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getParentId() {
        return parentId;
    }

    public static DataCheckTypeEnum getEnum(int value) {
        for (DataCheckTypeEnum e : DataCheckTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return DataCheckTypeEnum.NONE;
    }

    public static DataCheckTypeEnum getEnumByName(String name) {
        for (DataCheckTypeEnum e : DataCheckTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return DataCheckTypeEnum.NONE;
    }
}
