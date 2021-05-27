package com.fisk.common.vo;

import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 基本的DVO，提供了VO和Entity之间的互相转换功能
 */
@Data
public abstract class BaseVO {

    /**
     * DTO转PO
     * @param entityClass PO对象的字节码
     * @param <T> PO对象的类型
     * @return PO对象
     */
    public <T> T toEntity(Class<T> entityClass) {
        return com.fisk.common.utils.BeanHelper.copyProperties(this, entityClass);
    }

    /**
     * 从Entity转为VO
     * @param entity 任意实体
     */
    public BaseVO(BaseEntity entity) {
        if(entity != null){
            BeanUtils.copyProperties(entity, this);
        }
    }

    public BaseVO() {
    }
}
