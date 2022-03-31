package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/1/6 16:11
 */
public enum PictureSuffixTypeEnum implements BaseEnum {

    /**
     * 图片后缀名
     */
    JGP(1,".jpg"),
    PNG(2,".png"),
    JPEG(3,".jpeg"),
    BMP(4,".bmp");

    PictureSuffixTypeEnum(int value, String name) {
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
