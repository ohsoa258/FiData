package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author WangYan
 * @Date 2022/6/30 15:01
 * @Version 1.0
 */
public enum MapTypeEnum implements BaseEnum {
    /**
     * 地图类型
     */
    GAODE_MAP(0,"高德地图"),

    BAIDU_MAP(1,"百度地图");

    MapTypeEnum(int value, String name) {
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
}
