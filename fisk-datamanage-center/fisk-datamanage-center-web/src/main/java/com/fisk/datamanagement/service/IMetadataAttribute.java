package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadataattribute.MetadataAttributeDTO;
import com.fisk.datamanagement.entity.MetadataAttributePO;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;

import java.util.List;

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
     * @param group
     * @return
     */
    ResultEnum delMetadataAttribute(Integer entityId, Integer group);

    /**
     * 元数据自定义脚本
     *
     * @param dtoList
     * @return
     */
    ResultEnum metadataCustomAttribute(List<MetadataAttributeDTO> dtoList);

    /**
     * 获取元数据下的技术属性
     * @param entityId
     * @return
     */
    List<MetadataAttributePO> getMetadataAttribute(Integer entityId);

}
