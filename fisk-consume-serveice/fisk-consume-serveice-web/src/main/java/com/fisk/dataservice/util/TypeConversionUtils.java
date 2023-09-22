package com.fisk.dataservice.util;

import com.fisk.common.core.mapstruct.EnumTypeConversionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.enums.RequestTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @Author Wangjian
 * @Date 2023/9/13 11:32
 * @Version 1.0
 * 类型转换器
 */
@Component
public class TypeConversionUtils extends EnumTypeConversionUtils {


    /**
     * dataServiceStatus枚举类型转换
     *
     * @param number 参数
     * @return {@link JsonTypeEnum}
     */
    public JsonTypeEnum intToJsonTypeEnum(Integer number){
        if (number == null){
            return null;
        }

        switch (number){
            case 1 :
                return JsonTypeEnum.ARRAY;
            case 2:
                return JsonTypeEnum.OBJECT;
            case 3:
                return JsonTypeEnum.NUMBER;
            case 4:
                return JsonTypeEnum.STRING;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * dataServiceStatus枚举类型转换
     *
     * @param number 参数
     * @return {@link RequestTypeEnum}
     */
    public RequestTypeEnum intToTypeEnum(Integer number){
        if (number == null){
            return null;
        }

        switch (number){
            case 1 :
                return RequestTypeEnum.NONE;
            case 2:
                return RequestTypeEnum.GET;
            case 3:
                return RequestTypeEnum.POST;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}