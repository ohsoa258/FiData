package com.fisk.common.core.utils;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间帮助类
 *
 * @author gy
 */
public class DateTimeUtils {

    public static String getNow() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getNowToShortDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * @return int
     * @description 计算两个日期之间天数差
     * @author dick
     * @date 2022/4/19 13:25
     * @version v1.0
     * @params minuend 被减数
     * @params subtraction 减数
     */
    public static int getTimeDifference_Day(String minuend, String subtraction) {
        int days = 0;
        try {
            // a-b=c,a减b等于c,a称为被减数,b称为减数,c称为差
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//年月日
            Date minuendDate = sdf.parse(minuend);
            Date subtractionDate = sdf.parse(subtraction);
            Calendar c1 = Calendar.getInstance();
            c1.setTime(minuendDate);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(subtractionDate);
            //算出天数
            days = c1.get(Calendar.DAY_OF_YEAR) - c2.get(Calendar.DAY_OF_YEAR);
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, "【getTimeDifference_Day】：" + ex);
        }
        return days;
    }
}
