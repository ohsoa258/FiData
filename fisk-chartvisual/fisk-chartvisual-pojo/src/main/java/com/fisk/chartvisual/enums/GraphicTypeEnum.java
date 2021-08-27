package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * 图形类型
 * @author JinXingWang
 */
public enum GraphicTypeEnum implements BaseEnum {
    LINE(1,"折线"),
    PIE(2,"饼图"),
    TABLE(3,"表格"),
    MATRIX(4,"矩阵"),
    BAR(5,"柱状图");
    GraphicTypeEnum(int value, String name) {
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
}
