package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum FileTypeEnum implements BaseEnum {

    /**
     * 图片格式
     */
    FILE_IMAGE(0, "图片格式"),
    /**
     * 文件格式
     */
    FILE(1, "文件格式");

    @EnumValue
    private final int value;
    private final String name;


    FileTypeEnum(int value, String name) {
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
