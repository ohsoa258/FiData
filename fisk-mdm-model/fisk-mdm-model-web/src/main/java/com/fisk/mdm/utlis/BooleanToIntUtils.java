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
    public int toInt(Boolean disabled){
        if (disabled == true){
            return 1;
        }

        return 0;
    }

    public Boolean toBoolean(Integer disabled){
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
        switch (type){
            case NOT_CREATED :
                return 0;
            case CREATED_SUCCESSFULLY:
                return 1;
            case CREATED_FAIL:
                return 2;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
