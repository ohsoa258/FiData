package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 值域检查-关键字包含类型
 * @date 2024/6/21 15:05
 */
public enum RangeCheckKeywordIncludeTypeEnum implements BaseEnum {
    NONE(0, "空"),
    CONTAINS_KEYWORDS(1, "包含关键字"),
    INCLUDE_KEYWORDS_BEFORE(2, "前包含关键字"),
    INCLUDE_KEYWORDS_AFTER(3, "后包含关键字");

    RangeCheckKeywordIncludeTypeEnum(int value, String name) {
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


    public static RangeCheckKeywordIncludeTypeEnum getEnum(int value) {
        for (RangeCheckKeywordIncludeTypeEnum e : RangeCheckKeywordIncludeTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return RangeCheckKeywordIncludeTypeEnum.NONE;
    }

    public static RangeCheckKeywordIncludeTypeEnum getEnumByName(String name) {
        for (RangeCheckKeywordIncludeTypeEnum e : RangeCheckKeywordIncludeTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return RangeCheckKeywordIncludeTypeEnum.NONE;
    }
}
