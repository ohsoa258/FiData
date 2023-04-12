package com.fisk.common.core.enums.sftp;


import com.fisk.common.core.enums.BaseEnum;

/**
 * @author SongJianJian
 */

public enum SftpAuthTypeEnum implements BaseEnum {

    USERNAME_PW_AUTH(0, "用户名"),
    RSA_AUTH(1, "RSA");



    SftpAuthTypeEnum(int value, String name) {
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
