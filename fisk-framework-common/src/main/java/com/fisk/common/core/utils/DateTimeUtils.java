package com.fisk.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    public static String getNowToShortDate(String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 将时间戳转换为时间,参数和返回值都是字符串
     *
     * @param s
     * @return res
     */
    public static String stampToDate(String s) {
        try {
            String res;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            long lt = new Long(s);
            Date date = new Date(lt);
            res = simpleDateFormat.format(date);
            return res;
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, "【stampToDate】：" + ex);
        }
    }

    /**
     * 时间转换成时间戳,参数和返回值都是字符串
     *
     * @param s
     * @return res
     * @throws ParseException
     */
    public static String dateToStamp(String s) {
        try {
            String res;
            //设置时间模版
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = simpleDateFormat.parse(s);
            long ts = date.getTime();
            res = String.valueOf(ts);
            return res;
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, "【dateToStamp】：" + ex);
        }
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");//年月日
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

    /**
     * @return java.lang.String
     * @description 指定日期加多少天
     * @author dick
     * @date 2022/4/21 18:38
     * @version v1.0
     * @params date
     * @params day
     */
    public static String getDateAddDay(String date, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cd = Calendar.getInstance();//获取一个Calendar对象
        try {
            cd.setTime(sdf.parse(date));//设置calendar日期
        } catch (ParseException ex) {
            throw new FkException(ResultEnum.ERROR, "【getNowAddDay】：" + ex);
        }
        cd.add(Calendar.DATE, day);//增加n天
        String format = sdf.format(cd.getTime());
        return format;
    }

    /**
     * 判断字符串是否为合法的日期格式
     *
     * @return
     */
    public static boolean isValidDate(List<String> dateList, String dateFormat) {
        if (CollectionUtils.isEmpty(dateList) || StringUtils.isEmpty(dateFormat)) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        for (String dateStr : dateList) {
            try {
                // yyyy-MM-dd HH:mm:ss ps：24小时和12小时制无法区分
                Date date = format.parse(dateStr);
                format.setLenient(false);
                //由于上述方法只能验证正常的日期格式，像诸如 0001-01-01、11-01-01，10001-01-01等无法校验，此处再添加校验年份是否合法
                String yearStr = dateStr.split("-")[0];
                if (yearStr.startsWith("0") || yearStr.length() != 4) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为合法的日期格式，返回不合法的数据
     *
     * @return
     */
    public static List<String> ValidDate(List<String> dateList, String dateFormat) {
        List<String> value = new ArrayList<>();
        if (CollectionUtils.isEmpty(dateList) || StringUtils.isEmpty(dateFormat)) {
            return value;
        }
        dateList = dateList.stream().distinct().collect(Collectors.toList());
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        for (String dateStr : dateList) {
            try {
                // yyyy-MM-dd HH:mm:ss ps：24小时和12小时制无法区分
                Date date = format.parse(dateStr);
                format.setLenient(false);
                //由于上述方法只能验证正常的日期格式，像诸如 0001-01-01、11-01-01，10001-01-01等无法校验，此处再添加校验年份是否合法
                String yearStr = dateStr.split("-")[0];
                if (yearStr.startsWith("0") || yearStr.length() != 4) {
                    value.add(dateStr);
                }
            } catch (Exception e) {
                value.add(dateStr);
            }
        }
        return value;
    }
}
