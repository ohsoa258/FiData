package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lock
 */
public enum FieldTypeEnum implements BaseEnum {
    /**
     * 数值类型
     */
    INT(1, "INT"),
    /**
     * 字符类型
     */
    STRING1(2, "VARCHAR"),
    /**
     * 时间戳类型
     */
    DATETIME(3, "TIMESTAMP"),
    /**
     * 文本类型
     */
    STRING2(4, "TEXT"),
    /**
     * 其他类型
     */
    OTHER(0, "其他类型");

    FieldTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public static FieldTypeEnum getValue(String name) {
        FieldTypeEnum[] fieldTypeEnums = values();
        for (FieldTypeEnum fieldTypeEnum : fieldTypeEnums) {
            String queryName = fieldTypeEnum.name;
            if (StringUtils.contains(name, queryName)) {
                return fieldTypeEnum;
            }
        }
        return null;
    }
}