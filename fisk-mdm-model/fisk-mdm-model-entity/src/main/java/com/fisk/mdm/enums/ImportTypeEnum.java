package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum ImportTypeEnum implements BaseEnum {

    /**
     * 页面手动输入
     */
    MANUALLY_ENTER(0,"手动输入"),
    /**
     * excel导入
     */
    EXCEL_IMPORT(1,"excel导入"),
    /**
     * NIFI同步
     */
    NIFI_SYNC(2,"NIFI同步");

    @EnumValue
    private final int value;
    private final String name;


    ImportTypeEnum(int value, String name) {
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
