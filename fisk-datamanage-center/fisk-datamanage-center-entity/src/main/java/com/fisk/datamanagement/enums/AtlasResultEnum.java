package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;
import com.fisk.common.core.response.ResultEnum;

/**
 * @author JianWenYang
 */
public enum AtlasResultEnum implements BaseEnum {

    /**
     * 请求成功
     */
    REQUEST_SUCCESS(200, "请求成功"),
    BAD_REQUEST(400,"错误请求"),
    NO_CONTENT(204,"没有内容"),
    NOT_SUPPORT(205,"暂不支持该类型数据查询"),
    UNKNOWN(999,"未知错误");

    AtlasResultEnum(int value, String name) {
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

    public static AtlasResultEnum getEnum(int code) {
        for (AtlasResultEnum enums : AtlasResultEnum.values()) {
            if (enums.getValue() == code) {
                return enums;
            }
        }
        return AtlasResultEnum.UNKNOWN;
    }

}
