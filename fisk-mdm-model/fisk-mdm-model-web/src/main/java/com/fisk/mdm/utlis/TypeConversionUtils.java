package com.fisk.mdm.utlis;

import com.fisk.common.core.mapstruct.EnumTypeConversionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEOperate.dto.RuleTypeEnum;
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
     * mdmStatus枚举类型转换
     *
     * @param status 状态
     * @return {@link MdmStatusTypeEnum}
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
            case 3:
                return AttributeStatusEnum.DELETE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * 属性发布状态转换
     */
    public AttributeSyncStatusEnum intToAttributeSyncStatusEnum(Integer value){
        if (value == null){
            return null;
        }

        return AttributeSyncStatusEnum.values()[value];
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
            case 9:
                return DataTypeEnum.BOOL;
            case 10:
                return DataTypeEnum.MONEY;
            case 11:
                return DataTypeEnum.TIME;
            case 12:
                return DataTypeEnum.TIMESTAMP;
            case 13:
                return DataTypeEnum.TEXTAREA;
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

    public ModelVersionStatusEnum intToModelVersionStatusEnum(Integer value){
        if (value == null){
            return null;
        }
        switch (value){
            case 0 :
                return ModelVersionStatusEnum.OPEN;
            case 1:
                return ModelVersionStatusEnum.LOCK;
            case 2:
                return ModelVersionStatusEnum.SUBMITTED;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    public ModelVersionTypeEnum intToModelVersionTypeEnum(Integer value){
        if (value == null){
            return null;
        }
        switch (value){
            case 0 :
                return ModelVersionTypeEnum.USER_CREAT;
            case 1:
                return ModelVersionTypeEnum.SYSTEM_CREAT;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    public static MapTypeEnum intToMapTypeEnum(Integer value){
        if (value == null){
            return null;
        }

        return MapTypeEnum.values()[value];
    }

    public static RuleTypeEnum intToRuleTypeEnum(Integer value){
        if (value == null){
            return null;
        }

        return RuleTypeEnum.values()[value];
    }
}