package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;

/**
 * @author JianWenYang
 */
public interface IMetadataAttribute {

    /**
     * 新增元数据属性
     *
     * @param object
     * @param entityId
     * @return
     */
    ResultEnum addMetadataAttribute(Object object, Integer entityId);

    /**
     * 删除元数据属性
     *
     * @param entityId
     * @return
     */
    ResultEnum delMetadataAttribute(Integer entityId);

}
