package com.fisk.common.service.mdmBEOperate.dto;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author WangYan
 * @Date 2022/6/23 11:01
 * @Version 1.0
 */
public enum RuleTypeEnum implements BaseEnum {


    /**
     * 编码创建规则类型
     */
    UUID(0,"随机生成"),
    AUTO_INCREMENT(1,"自增"),
    TIMESTAMP(2,"时间戳"),
    FIXED_VALUE(3,"固定值"),
    FIELD(4,"字段");

    @EnumValue
    private final int value;
    private final String name;

    RuleTypeEnum(int value, String name) {
        this.value=value;
        this.name=name;
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
