package com.fisk.common.core.enums.emailwarnlevel;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 邮件预警级别枚举类
 */
public enum EmailWarnLevelEnum implements BaseEnum {

    //预警程度 红>橙>黄>绿
    RED("红色预警", 0),
    ORANGE("橙色预警", 1),
    YELLOW("黄色预警", 2),
    GREEN("绿色预警", 3),
    ;

    EmailWarnLevelEnum(String name, int value) {
        this.name = name;
        this.value = value;
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

    public static EmailWarnLevelEnum getEnum(String name) {
        for (EmailWarnLevelEnum e : EmailWarnLevelEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return EmailWarnLevelEnum.GREEN;
    }

    public static EmailWarnLevelEnum getEnum(int value) {
        for (EmailWarnLevelEnum e : EmailWarnLevelEnum.values()) {
            if (value == e.value)
                return e;
        }
        return EmailWarnLevelEnum.GREEN;
    }
}
