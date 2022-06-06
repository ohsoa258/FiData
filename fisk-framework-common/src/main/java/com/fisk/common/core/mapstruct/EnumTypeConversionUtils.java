package com.fisk.common.core.mapstruct;

import com.fisk.common.core.enums.BaseEnum;
import org.springframework.stereotype.Component;

/**
 * @Author WangYan, GuoYu
 * @Date 2022/4/21 16:14
 * @Version 1.0
 * 类型转换器
 */
@Component
public class EnumTypeConversionUtils {

    /**
     * bool转int
     * 0：false 1:true
     *
     * @param value Bool value
     * @return int value
     */
    public Integer boolToInt(Boolean value) {
        if (value == null) {
            return null;
        }

        return value ? 1 : 0;
    }

    /**
     * int转bool
     * 0：false 1:true
     *
     * @param value int value
     * @return bool value
     */
    public Boolean intToBoolean(Integer value) {
        if (value == null) {
            return null;
        }

        return value.equals(1);
    }

    /**
     * 获取枚举的value属性
     *
     * @param type 枚举项
     * @return value
     */
    public Integer getEnumValue(BaseEnum type) {
        if (type == null) {
            return null;
        }

        return type.getValue();
    }

    /**
     * 获取枚举的name属性
     *
     * @param type 枚举项
     * @return value
     */
    public String getEnumName(BaseEnum type) {
        if (type == null) {
            return null;
        }
        return type.getName();
    }


}
