package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 * @date 2022-09-07 16:46
 */
public enum ApiParameterTypeEnum implements BaseEnum {

    /**
     * 常量
     */
    CONST(1, "常量"),

    EXPRESSION(2, "表达式"),

    SCRIPT(3, "脚本");


    private final int value;
    private final String name;

    ApiParameterTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
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
