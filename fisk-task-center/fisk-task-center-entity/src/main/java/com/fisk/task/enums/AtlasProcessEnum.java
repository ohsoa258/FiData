package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/15 11:34
 * Description:
 */
public enum AtlasProcessEnum implements BaseEnum {
    /**
     * atlas构建实体连接的类型
     */
    instance(0,"rdbms_instance"),
    db(1,"rdbms_db"),
    table(2,"rdbms_table"),
    column(3,"rdbms_column"),
    higherInstance(4,"instance"),
    higherDb(5,"db"),
    higherTable(6,"table");

    AtlasProcessEnum(int value, String name) {
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
}
