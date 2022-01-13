package com.fisk.dataservice.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version v1.0
 * @description 图形类型
 * @date 2022/1/6 14:51
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
