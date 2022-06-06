package com.fisk.common.server.ocr;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 * @version 1.0
 * @description TODO
 * @date 2022/5/31 16:32
 */
public enum OcrServerEnum implements BaseEnum {

    /**
     * 支持的所有OCR服务
     */
    ALIYUN(0, "aliyun");

    private final String name;
    private final int value;

    OcrServerEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }
}
