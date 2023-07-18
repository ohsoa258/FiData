package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 规范检查类型
 * @date 2022/5/31 16:33
 */
public enum StandardCheckTypeEnum implements BaseEnum {

    NONE(0, "空"),
    DATE_FORMAT(1, "日期格式"),
    CHARACTER_PRECISION_LENGTH_RANGE(2, "字符精度长度范围"),
    URL_ADDRESS(3, "URL地址"),
    BASE64_BYTE_STREAM(4, "BASE64字节流");

    StandardCheckTypeEnum(int value, String name) {
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


    public static StandardCheckTypeEnum getEnum(int value) {
        for (StandardCheckTypeEnum e : StandardCheckTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return StandardCheckTypeEnum.NONE;
    }

    public static StandardCheckTypeEnum getEnumByName(String name) {
        for (StandardCheckTypeEnum e : StandardCheckTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return StandardCheckTypeEnum.NONE;
    }
}
