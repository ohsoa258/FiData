package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-09-11
 * @Description:
 */
public enum SpecialTypeEnum implements BaseEnum {
    /**
     * 0:无 1:物料主数据 2:通知单 3:库存状态变更
     */
    NONE(0,"无"),
    KSF_ITEM_DATA(1,"物料主数据"),
    KSF_NOTICE(2,"通知单"),
    KSF_INVENTORY_STATUS_CHANGES(3,"库存状态变更"),

    KSF_ACKNOWLEDGEMENT(4,"确认单");
    SpecialTypeEnum(int value, String name) {
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

    public static SpecialTypeEnum getEnum(int value) {
        for (SpecialTypeEnum e : SpecialTypeEnum.values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return SpecialTypeEnum.NONE;
    }
}
