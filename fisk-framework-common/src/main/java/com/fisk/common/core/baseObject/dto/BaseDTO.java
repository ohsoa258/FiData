package com.fisk.common.core.baseObject.dto;

import com.fisk.common.core.baseObject.entity.BaseEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @author Lock
 * 基本的DTO，提供了DTO和Entity之间的互相转换功能
 */
@Data
public abstract class BaseDTO {

    /**
     * DTO转PO
     * @param entityClass PO对象的字节码
     * @param <T> PO对象的类型
     * @return PO对象
     */
    public <T> T toEntity(Class<T> entityClass) {
        return com.fisk.common.core.utils.BeanHelper.copyProperties(this, entityClass);
    }

    /**
     * 从Entity转为DTO
     * @param entity 任意实体
     */
    public BaseDTO(BaseEntity entity) {
        if(entity != null){
            BeanUtils.copyProperties(entity, this);
        }
    }

    public BaseDTO() {
    }
}
