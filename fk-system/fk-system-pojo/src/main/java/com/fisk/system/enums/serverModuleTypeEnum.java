package com.fisk.system.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2021/11/4 14:03
 */
public enum  serverModuleTypeEnum implements BaseEnum {

    DATA_INPUT("数据接入",0),

    DATA_MODEL("数据建模",1);

    private final String name;
    private final int value;

    serverModuleTypeEnum(String name,int value){
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
