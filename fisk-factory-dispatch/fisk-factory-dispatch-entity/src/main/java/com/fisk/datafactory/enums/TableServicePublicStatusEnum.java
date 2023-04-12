package com.fisk.datafactory.enums;

import com.fisk.common.core.enums.BaseEnum;
import com.fisk.task.enums.OlapTableEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SongJianJian
 */
public enum TableServicePublicStatusEnum implements BaseEnum {

    PUBLIC_NO(0, "未发布"),
    PUBLIC_YES(1, "已发布"),
    PUBLIC_FAIL(2, "发布失败");

    TableServicePublicStatusEnum(int value, String name) {
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

    public static TableServicePublicStatusEnum getValue(String name) {
        TableServicePublicStatusEnum[] carTypeEnums = values();
        for (TableServicePublicStatusEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }
}
