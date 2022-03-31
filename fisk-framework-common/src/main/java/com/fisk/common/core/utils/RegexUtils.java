package com.fisk.common.core.utils;

import com.fisk.common.core.constants.RegexPatterns;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lock
 * @date 2021/5/10 10:13
 */
public class RegexUtils {

    /**
     * 是否符合手机格式
     * @param phone 要校验的手机号
     * @return true:符合，false：不符合
     */
    public static boolean isPhone(String phone){
        return matches(phone, RegexPatterns.PHONE_REGEX);
    }
    /**
     * 是否符合邮箱格式
     * @param email 要校验的邮箱
     * @return true:符合，false：不符合
     */
    public static boolean isEmail(String email){
        return matches(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * 是否符合验证码格式
     * @param code 要校验的验证码
     * @return true:符合，false：不符合
     */
    public static boolean isCodeValid(String code){
        return matches(code, RegexPatterns.VERIFY_CODE_REGEX);
    }

    private static boolean matches(String str, String regex){
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.matches(regex);
    }

}
