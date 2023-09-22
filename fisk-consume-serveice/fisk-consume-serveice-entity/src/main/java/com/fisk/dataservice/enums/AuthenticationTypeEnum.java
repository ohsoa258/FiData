package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-09-11
 * @Description:
 */
public enum AuthenticationTypeEnum implements BaseEnum {
    /**
     * 0:无身份验证 1:基础验证 2:JWT 3:Bearer Token 4:OAuth2.0 5:API key
     */
    NONE_VALIDATION(0,"无身份验证"),
    BASIC_VALIDATION(1,"基础验证"),
    JWT_VALIDATION(2,"JWT"),
    BEARER_TOKEN_VALIDATION(3,"Bearer Token"),
    OAUTH_TWO_VALIDATION(4,"OAuth2.0"),
    API_KEY_VALIDATION(5,"API key");
    AuthenticationTypeEnum(int value, String name) {
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

    public static AuthenticationTypeEnum getEnum(int value) {
        for (AuthenticationTypeEnum e : AuthenticationTypeEnum.values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return AuthenticationTypeEnum.NONE_VALIDATION;
    }
}
