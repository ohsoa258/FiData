package com.fisk.common.core.utils;

import com.fisk.common.core.constants.RegexPatterns;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * @author Lock
 * @date 2021/5/10 10:13
 */
public class RegexUtils {

    /**
     * 是否符合手机格式
     *
     * @param phone 要校验的手机号
     * @return true:符合，false：不符合
     */
    public static boolean isPhone(String phone) {
        return matches(phone, RegexPatterns.PHONE_REGEX);
    }

    /**
     * 是否符合邮箱格式
     *
     * @param email 要校验的邮箱
     * @return true:符合，false：不符合
     */
    public static boolean isEmail(String email) {
        return matches(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * 是否符合验证码格式
     *
     * @param code 要校验的验证码
     * @return true:符合，false：不符合
     */
    public static boolean isCodeValid(String code) {
        return matches(code, RegexPatterns.VERIFY_CODE_REGEX);
    }

    /**
     * 判断两个数组内容是否一致
     *
     * @param list1
     * @param list2
     * @return true:符合，false：不符合
     */
    public static List<String> subtractValid(List<String> list1, List<String> list2, boolean isDistinct) {
        if (isDistinct) {
            list1 = list1.stream().distinct().collect(Collectors.toList());
            list2 = list2.stream().distinct().collect(Collectors.toList());
        }
        List<String> subtract = (List<String>) CollectionUtils.subtract(list1, list2);
        return subtract;
    }

    /**
     * 自定义函数去重: 用于stream流根据对象指定字段去重
     *
     * @param keyExtractor 去重的对象
     * @param <T>          泛型
     * @return 返回的实体对象
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private static boolean matches(String str, String regex) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches(regex);
    }

    public static boolean isCharValid(String fieldType) {
        boolean isChar = true;
        if (StringUtils.isEmpty(fieldType)) {
            return isChar;
        }
        fieldType = fieldType.toLowerCase();
        if (fieldType.contains("int")) {
            fieldType = "int";
        }
        if (fieldType.contains("float")) {
            fieldType = "float";
        }
        String timeType = "date";

        // 浮点型
        List<String> floatType = new ArrayList<>();
        floatType.add("double");

        // 文本类型
        List<String> textTpye = new ArrayList<>();
        textTpye.add("text");

        // 字符型
        List<String> charType = new ArrayList<>();
        charType.add("varchar");
        charType.add("char");

        // Number型
        // 整型
        List<String> integerType = new ArrayList<>();
        integerType.add("tinyint");
        integerType.add("smallint");
        integerType.add("mediumint");
        integerType.add("int");
        integerType.add("integer");
        integerType.add("bigint");
        // 精确数值型
        List<String> accurateType = new ArrayList<>();
        accurateType.add("decimal");
        accurateType.add("numeric");
        // 货币、近似数值型
        List<String> otherType = new ArrayList<>();
        otherType.add("money");
        otherType.add("smallmoney");
        otherType.add("float");
        otherType.add("real");

        // boolean类型长度放开
        if (integerType.contains(fieldType.toLowerCase())) {
            isChar = false;
        } else if (textTpye.contains(fieldType.toLowerCase())) {
            isChar = true;
        } else if (accurateType.contains(fieldType.toLowerCase()) || otherType.contains(fieldType.toLowerCase())) {
            isChar = false;
        } else if (charType.contains(fieldType.toLowerCase())) {
            isChar = true;
        } else if (fieldType.toLowerCase().contains(timeType)) {
            isChar = false;
        }
        return isChar;
    }

    /**
     * @return boolean
     * @description 集合中是否包含某个值，忽略大小写
     * @author dick
     * @date 2022/10/21 11:33
     * @version v1.0
     * @params list
     * @params str
     */
    public static boolean isContains(List<String> list, String str) {
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        return list.stream().anyMatch(str::equalsIgnoreCase);
    }

    /**
     * @return int
     * @description 判断是否是正整数，不是则返回null
     * @author dick
     * @date 2023/4/14 14:19
     * @version v1.0
     * @params s
     */
    public static Integer isNumeric(Object s) {
        if (s != null && !"".equals(s.toString().trim()) && s.toString().matches("^[0-9]*$")) {
            return Integer.parseInt(s.toString());
        } else {
            return null;
        }
    }

    /**
     * @return boolean
     * @description 是否是Base64编码
     * @author dick
     * @date 2023/7/4 17:09
     * @version v1.0
     * @params input
     */
    public static boolean isBase64String(List<String> inputs, boolean isSkipNullCheck) {
        try {
            // 尝试解码字符串，如果不抛出异常则说明是有效的 Base64 编码
            for (String input : inputs) {
                if (StringUtils.isEmpty(input) && !isSkipNullCheck) {
                    return false;
                } else {
                    byte[] decodedBytes = Base64.getDecoder().decode(input);
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            // 解码失败，说明不是 Base64 编码
            return false;
        }
    }

    /**
     * @return boolean
     * @description 是否是URL格式
     * @author dick
     * @date 2023/7/4 17:16
     * @version v1.0
     * @params inputs
     * @params isSkipNullCheck
     */
    public static boolean isValidURL(List<String> inputs, boolean isSkipNullCheck) {
        try {
            // 尝试创建 URL 对象，如果不抛出异常则说明是有效的 URL。只验证URL格式
            for (String input : inputs) {
                if (StringUtils.isEmpty(input) && !isSkipNullCheck) {
                    return false;
                } else {
                    new URL(input);
                }
            }
            return true;
        } catch (MalformedURLException e) {
            // URL 格式不正确，不是有效的 URL
            return false;
        }
    }

    /**
     * @return boolean
     * @description 是否满足正则表达式验证
     * @author dick
     * @date 2023/7/4 17:16
     * @version v1.0
     * @params inputs
     * @params isSkipNullCheck
     */
    public static boolean isValidPattern(List<String> inputs, String regex, boolean isSkipNullCheck) {
        try {
            Pattern pattern = Pattern.compile(regex);
            for (String str : inputs) {
                if (StringUtils.isEmpty(str) && !isSkipNullCheck) {
                    return false;
                } else {
                    Matcher matcher = pattern.matcher(str);
                    if (!matcher.matches()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

}
