package com.fisk.common.service.sqlparser.model;

/**
 * @author gy
 * @version 1.0
 * @description 表类型枚举
 * @date 2022/12/6 17:25
 */
public enum TableTypeEnum {
    /**
     * 表类型枚举
     */
    UNION(0),
    SUBQUERY(1),
    Expr(2),
    JOIN(3),
    NONE(99);

    private final int type;

    TableTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
