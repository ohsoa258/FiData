package com.fisk.datamanagement.utils;

import com.fisk.common.core.mapstruct.EnumTypeConversionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.enums.ValueRangeTypeEnum;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.enums.RequestTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @Author Wangjian
 * @Date 2024/1/30 11:32
 * @Version 1.0
 * 类型转换器
 */
@Component
public class TypeConversionUtils extends EnumTypeConversionUtils {


    /**
     * ValueRangeTypeEnum枚举类型转换
     *
     * @param number 参数
     * @return {@link ValueRangeTypeEnum}
     */
    public ValueRangeTypeEnum intToValueRangeTypeEnum(Integer number){
        if (number == null){
            return null;
        }

        switch (number){
            case 0 :
                return ValueRangeTypeEnum.NONE;
            case 1 :
                return ValueRangeTypeEnum.DATASET;
            case 2:
                return ValueRangeTypeEnum.VALUE;
            case 3:
                return ValueRangeTypeEnum.VALUE_RANGE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}