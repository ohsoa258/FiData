package com.fisk.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2021/11/4 14:03
 */
public enum  serverModuleTypeEnum implements BaseEnum {

    DATA_INPUT("数据接入",0),

    DATA_MODEL("数据建模",1),

    DATA_DISPATCH("数据调度",2),

    DATA_SERVICE("Api服务",3),

    DATA_MANAGEMENT("数据治理",4);

    private final String name;
    @EnumValue
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
