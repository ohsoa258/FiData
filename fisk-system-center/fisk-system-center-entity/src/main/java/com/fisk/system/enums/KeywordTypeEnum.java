package com.fisk.system.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author Lock
 */
public enum KeywordTypeEnum implements BaseEnum {

    /**
     * mysql关键字
     */
    MYSQL("mysql关键字", 1),
    /**
     * sqlserver关键字
     */
    SQLSERVER("sqlserver关键字", 2),
    /**
     * pgsql关键字
     */
    PGSQL("pgsql关键字", 3),
    /**
     * doris关键字
     */
    DORIS("doris关键字", 4);

    private final String name;
    private final int value;

    KeywordTypeEnum(String name, int value) {
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
