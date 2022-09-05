package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum ApiConditionEnum implements BaseEnum {

    /**
     * token
     */
    TOKEN(1, "Token"),
    PAGENUM(2, "PageNum"),

    MAX(3, "Max"),
    Min(4, "Min"),
    SUM(5, "Sum"),

    GETDATE(6, "GetDate"),
    GETDATETIME(7, "GetDateTime");

    private final int value;
    private final String name;

    ApiConditionEnum(int value, String name) {
        this.name = name;
        this.value = value;
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
