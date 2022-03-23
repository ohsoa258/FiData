package com.fisk.system.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum KeywordTypeEnum implements BaseEnum {

    MYSQL("mysql关键字",1),

    SQLSERVER("sqlserver关键字",2),

    PGSQL("pgsql关键字",3),

    DORIS("doris关键字",4);

    private final String name;
    private final int value;

    KeywordTypeEnum(String name, int value){
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
