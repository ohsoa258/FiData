package com.fisk.mdm.utlis;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.enums.MdmStatusTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @Author WangYan
 * @Date 2022/4/21 16:14
 * @Version 1.0
 * 类型转换器
 */
@Component
public class BooleanToIntUtils {

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
}
