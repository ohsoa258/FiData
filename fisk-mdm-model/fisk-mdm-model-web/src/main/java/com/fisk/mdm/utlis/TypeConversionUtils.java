package com.fisk.mdm.utlis;

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
public class TypeConversionUtils {

    /**
     *  0：false 1:true
     * @param disabled
     * @return
     */
    public Integer toInt(Boolean disabled){
        if (disabled == null){
            return null;
        }

        if (disabled == true){
            return 1;
        }

        return 0;
    }

    public Boolean toBoolean(Integer disabled){
        if (disabled == null){
            return null;
        }

        if (disabled.equals(1)){
            return true;
        }else {
            return false;
        }
    }

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

    public Integer typeEnumToInt(MdmStatusTypeEnum type){
        if (type == null){
            return null;
        }

        switch (type){
            case NOT_CREATED :
                return MdmStatusTypeEnum.NOT_CREATED.getValue();
            case CREATED_SUCCESSFULLY:
                return MdmStatusTypeEnum.CREATED_SUCCESSFULLY.getValue();
            case CREATED_FAIL:
                return MdmStatusTypeEnum.CREATED_FAIL.getValue();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    public String typeEnumToString(MdmStatusTypeEnum type){
        if (type == null){
            return null;
        }

        switch (type){
            case NOT_CREATED :
                return MdmStatusTypeEnum.NOT_CREATED.getName();
            case CREATED_SUCCESSFULLY:
                return MdmStatusTypeEnum.CREATED_SUCCESSFULLY.getName();
            case CREATED_FAIL:
                return MdmStatusTypeEnum.CREATED_FAIL.getName();
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

    public Integer attributeStatusEnumToInt(AttributeStatusEnum attributeStatusEnum){
        if (attributeStatusEnum == null){
            return null;
        }
        switch (attributeStatusEnum){
            case INSERT :
                return AttributeStatusEnum.INSERT.getValue();
            case UPDATE:
                return AttributeStatusEnum.UPDATE.getValue();
            case SUBMITTED:
                return AttributeStatusEnum.SUBMITTED.getValue();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    public String attributeStatusEnumToString(AttributeStatusEnum attributeStatusEnum){
        if (attributeStatusEnum == null){
            return null;
        }
        switch (attributeStatusEnum){
            case INSERT :
                return AttributeStatusEnum.INSERT.getName();
            case UPDATE:
                return AttributeStatusEnum.UPDATE.getName();
            case SUBMITTED:
                return AttributeStatusEnum.SUBMITTED.getName();
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

    public Integer attributeSyncStatusEnumToInt(AttributeSyncStatusEnum attributeSyncStatusEnum){
        if (attributeSyncStatusEnum == null){
            return null;
        }
        switch (attributeSyncStatusEnum){
            case SUCCESS :
                return AttributeSyncStatusEnum.SUCCESS.getValue();
            case ERROR:
                return AttributeSyncStatusEnum.ERROR.getValue();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    public String attributeSyncStatusEnumToString(AttributeSyncStatusEnum attributeSyncStatusEnum){
        if (attributeSyncStatusEnum == null){
            return null;
        }
        switch (attributeSyncStatusEnum){
            case SUCCESS :
                return AttributeSyncStatusEnum.SUCCESS.getName();
            case ERROR:
                return AttributeSyncStatusEnum.ERROR.getName();
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

    public Integer dataTypeEnumToInt(DataTypeEnum dataTypeEnum){
        if (dataTypeEnum == null){
            return null;
        }
        switch (dataTypeEnum){
            case TEXT :
                return DataTypeEnum.TEXT.getValue();
            case DATE:
                return DataTypeEnum.DATE.getValue();
            case NUMERICAL:
                return DataTypeEnum.NUMERICAL.getValue();
            case DOMAIN:
                return DataTypeEnum.DOMAIN.getValue();
            case LATITUDE_COORDINATE:
                return DataTypeEnum.LATITUDE_COORDINATE.getValue();
            case OCR:
                return DataTypeEnum.OCR.getValue();
            case FILE:
                return DataTypeEnum.FILE.getValue();
            case POI:
                return DataTypeEnum.POI.getValue();
            case FLOAT:
                return DataTypeEnum.FLOAT.getValue();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    public String dataTypeEnumToString(DataTypeEnum dataTypeEnum){
        if (dataTypeEnum == null){
            return null;
        }
        switch (dataTypeEnum){
            case TEXT :
                return DataTypeEnum.TEXT.getName();
            case DATE:
                return DataTypeEnum.DATE.getName();
            case NUMERICAL:
                return DataTypeEnum.NUMERICAL.getName();
            case DOMAIN:
                return DataTypeEnum.DOMAIN.getName();
            case LATITUDE_COORDINATE:
                return DataTypeEnum.LATITUDE_COORDINATE.getName();
            case OCR:
                return DataTypeEnum.OCR.getName();
            case FILE:
                return DataTypeEnum.FILE.getName();
            case POI:
                return DataTypeEnum.POI.getName();
            case FLOAT:
                return DataTypeEnum.FLOAT.getName();
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

    public String mdmTypeEnumToString(MdmTypeEnum mdmTypeEnum){
        if (mdmTypeEnum == null){
            return null;
        }
        switch (mdmTypeEnum){
            case CODE :
                return MdmTypeEnum.CODE.getName();
            case NAME:
                return MdmTypeEnum.NAME.getName();
            case BUSINESS_FIELD:
                return MdmTypeEnum.BUSINESS_FIELD.getName();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }


}
