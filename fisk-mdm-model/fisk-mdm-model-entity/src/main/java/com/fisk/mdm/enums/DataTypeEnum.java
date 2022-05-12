package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/4/5 12:11
 * 数据类型
 */
public enum DataTypeEnum implements BaseEnum {

    /**
     * 简单数据类型
     * 说明: 域字段相当于外键
     */
    /**
     * pg: varchar
     */
    TEXT(0, "文本"),
    /**
     * pg: date
     * yyyy-MM-dd
     */
    DATE(1, "日期"),
    /**
     * pg:int4
     */
    NUMERICAL(2, "数值"),
    /**
     * int4
     */
    DOMAIN(3, "域字段"),
    /**
     * float4
     */
    FLOAT(8, "浮点型"),

    BOOL(9,"布尔型"),

    /**
     * pg: money
     */
    MONEY(10,"货币"),

    /**
     * pg: time
     * HH-mm-ss
     */
    TIME(11,"时间"),

    /**
     * pg: timestamp
     * yyyy-MM-dd HH-mm-ss
     */
    TIMESTAMP(12,"日期时间"),

    /**
     * 复杂数据类型
     * 说明:
     * 经纬度坐标: 使用域字段关联系统级别的经纬度表。经纬度表字段：经纬度（逗号隔开），精度，维度，地图类型
     * OCR（图文识别）: 使用域字段关联系统级别的OCR表。OCR表：文件地址，文件名称，识别内容，保存内容
     * 文件 : 使用域字段管理系统级别的文件表。文件表：存储文件地址
     * pg: int4
     */
    LATITUDE_COORDINATE(4, "经纬度坐标"),
    OCR(5,"OCR"),
    FILE(6,"文件"),
    POI(7,"POI");

    DataTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @EnumValue
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

    public static DataTypeEnum getValue(String name) {
        DataTypeEnum[] carTypeEnums = values();
        for (DataTypeEnum carTypeEnum : carTypeEnums) {
            String queryName=carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return DataTypeEnum.TEXT;
    }

}
