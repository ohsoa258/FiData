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
    FOLDER(1, "FOLDER"),
    TABLE(2, "TABLE"),
    VIEW(3, "VIEW"),
    FIELD(4, "FIELD");

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
