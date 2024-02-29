package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 *
 * @author JinXingWang
 */
public enum DataAssetsTypeEnum implements BaseEnum {
    LABEL(1,"LABEL"),
    LABEL_CATEGORY(2,"LABEL_CATEGORY"),
    GLOSSARY(3,"GLOSSARY"),
    GLOSSARY_CATEGORY(4,"GLOSSARY_CATEGORY"),
    META_DATA(5,"META_DATA");

    DataAssetsTypeEnum(int value, String name) {
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
