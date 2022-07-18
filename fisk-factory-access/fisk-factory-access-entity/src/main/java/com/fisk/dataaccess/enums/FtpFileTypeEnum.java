package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
    XLS_FILE(2, "xls"),
    XLSX_FILE(3, "xlsx"),
    CSV_FILE(1, "csv"),
    OTHER_FILE(0, "其他类型");

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

    public static FtpFileTypeEnum getValue(int value) {
        FtpFileTypeEnum[] carTypeEnums = values();
        for (FtpFileTypeEnum carTypeEnum : carTypeEnums) {
            int value1 = carTypeEnum.value;
            if (value1 == value) {
                return carTypeEnum;
            }
        }
        return FtpFileTypeEnum.OTHER_FILE;
    }

    /**
     * 判断文件后缀名
     *
     * @param fileName a.xlsx
     * @return boolean
     * @author Lock
     * @date 2022/7/18 11:38
     */
    public static   boolean judgeSuffixList(String fileName) {
        boolean flag = true;
        List<String> list = new ArrayList<>();
        list.add(XLS_FILE.name);
        list.add(XLSX_FILE.name);
        list.add(CSV_FILE.name);
        for (String s : list) {
            if (StringUtils.containsIgnoreCase(fileName,s)) {
                flag = false;
                break;
            }
        }
        return flag;
    }
}
