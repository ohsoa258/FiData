package com.fisk.system.enums.ssologin;

import com.fisk.common.core.enums.BaseEnum;

public enum SSORoleInfoEnum implements BaseEnum {

    /**
     * 角色类型  该枚举的value值参考各环境的dmp_system_db  tb_role_info的值
     */
    NORMAL_USER(3,"普通用户"),
    ADMIN(1,"管理员"),
    SUPER_ADMIN(2,"超级管理员");

    SSORoleInfoEnum(int value, String name) {
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
