package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2024-06-19
 * @Description:
 */
public enum DownLoadTypeEnum{
    /**
     * 导出方式
     */
    ALL("1","全部"),
    SECTION("2","部分");

    DownLoadTypeEnum(String value, String name) {
        this.name = name;
        this.value = value;
    }

    private final String value;
    private final String name;

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static DownLoadTypeEnum getEnum(String code) {
        for (DownLoadTypeEnum enums : DownLoadTypeEnum.values()) {
            if (enums.getValue() == code) {
                return enums;
            }
        }
        return null;
    }
}
