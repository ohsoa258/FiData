package com.fisk.mdm.utlis;

import com.fisk.common.core.mapstruct.EnumTypeConversionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.enums.*;
import org.springframework.stereotype.Component;

/**
 * @Author WangYan
 * @Date 2022/4/21 16:14
 * @Version 1.0
 * 类型转换器
 */
@Component
public class TypeConversionUtils extends EnumTypeConversionUtils {

    /**
     * status 转换
     * @param status
     * @return
     */
    public MdmStatusTypeEnum intToTypeEnum(Integer status){
        if (status == null){
            return null;
        }

        switch (status){
            case 0 :
                return MdmStatusTypeEnum.NOT_CREATED;
            case 1:
                return MdmStatusTypeEnum.CREATED_SUCCESSFULLY;
            case 2:
                return MdmStatusTypeEnum.CREATED_FAIL;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }


    /**
     * 属性状态转换
     */
    public AttributeStatusEnum intToAttributeStatusEnum(Integer value){
        if (value == null){
            return null;
        }
        switch (value){
            case 0 :
                return AttributeStatusEnum.INSERT;
            case 1:
                return AttributeStatusEnum.UPDATE;
            case 2:
                return AttributeStatusEnum.SUBMITTED;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 属性提交状态转换
     */
    public AttributeSyncStatusEnum intToAttributeSyncStatusEnum(Integer value){
        if (value == null){
            return null;
        }

        switch (value){
            case 0 :
                return AttributeSyncStatusEnum.SUCCESS;
            case 1:
                return AttributeSyncStatusEnum.ERROR;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 数据类型枚举转换
     */

    public DataTypeEnum intToDataTypeEnum(Integer value){
        if (value == null){
            return null;
        }

        switch (value){
            case 0 :
                return DataTypeEnum.TEXT;
            case 1:
                return DataTypeEnum.DATE;
            case 2:
                return DataTypeEnum.NUMERICAL;
            case 3:
                return DataTypeEnum.DOMAIN;
            case 4:
                return DataTypeEnum.LATITUDE_COORDINATE;
            case 5:
                return DataTypeEnum.OCR;
            case 6:
                return DataTypeEnum.FILE;
            case 7:
                return DataTypeEnum.POI;
            case 8:
                return DataTypeEnum.FLOAT;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * mdm类型枚举转换
     */
    public MdmTypeEnum intToMdmTypeEnum(Integer value){
        if (value == null){
            return null;
        }

        switch (value){
            case 0 :
                return MdmTypeEnum.CODE;
            case 1:
                return MdmTypeEnum.NAME;
            case 2:
                return MdmTypeEnum.BUSINESS_FIELD;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}