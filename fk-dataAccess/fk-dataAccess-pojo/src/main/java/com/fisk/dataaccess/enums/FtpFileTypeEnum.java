package com.fisk.dataaccess.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 * @version 1.0
 * @description ftp文件后缀名类型
 * @date 2022/1/5 11:48
 */
public enum FtpFileTypeEnum implements BaseEnum {

    /**
     * 文件后缀名类型
     */
    XLS_FILE(1, "xls"),
    XLSX_FILE(2, "xlsx"),
    CSV_FILE(3, "csv");

    FtpFileTypeEnum(int value, String name) {
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
