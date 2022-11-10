package com.fisk.common.core.enums.fidatadatasource;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description FiData数据源树层级类型
 * @date 2022/6/15 11:57
 */
public enum LevelTypeEnum implements BaseEnum {

    NONE(0, "NONE"),
    BASEFOLDER(1, "BASEFOLDER"),
    FOLDER(2, "FOLDER"),
    DATABASE(3, "DATABASE"),
    TABLE(4, "TABLE"),
    VIEW(5, "VIEW"),
    FIELD(6, "FIELD");

    LevelTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public static LevelTypeEnum getEnum(int value){
        for (LevelTypeEnum e:LevelTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return LevelTypeEnum.NONE;
    }
}
