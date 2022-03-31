package com.fisk.common.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间帮助类
 * @author gy
 */
public class DateTimeUtils {

    public static String getNow() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
