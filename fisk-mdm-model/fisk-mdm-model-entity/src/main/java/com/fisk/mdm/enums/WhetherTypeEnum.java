package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author WangYan
 * @date 2022/4/2 18:17
 */
public enum WhetherTypeEnum {

    /**
     * 0:false
     * 1:true
     */
    FALSE(0,false),
    TRUE(1,true);

    WhetherTypeEnum(int value, Boolean name) {
        this.name = name;
        this.value = value;
    }

    @EnumValue
    private final int value;
    private final Boolean name;


    public int getValue() {
        return value;
    }

    public Boolean getName() {
        return name;
    }
}
