package com.fisk.common.core.enums.task;


import com.fisk.common.core.enums.BaseEnum;

public enum SynchronousTypeEnum implements BaseEnum {

    TOPGODS(0,"toPgOds"),
    PGTOPG(1,"pgToPg"),
    PGTODORIS(2,"PgToDoris");


    private final String name;
    private final int value;

    SynchronousTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
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
