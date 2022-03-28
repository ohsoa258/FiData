package com.fisk.dataservice.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version v1.0
 * @description api状态枚举
 * @date 2022/1/6 14:51
 */
public enum ApiStateTypeEnum  implements BaseEnum {

    /**
     * api状态
     */
    Disable(0,"禁用"),
    Enable(1,"启用");

    ApiStateTypeEnum(int value, String name) {
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
