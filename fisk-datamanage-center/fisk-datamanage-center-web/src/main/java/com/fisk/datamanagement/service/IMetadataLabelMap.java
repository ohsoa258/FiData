package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadatalabelmap.MetadataLabelMapParameter;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IMetadataLabelMap {

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    ResultEnum addMetadataLabelMap(MetadataLabelMapParameter dto);

    /**
     * 删除
     *
     * @param metadataEntityId
     * @return
     */
    ResultEnum delMetadataLabelMap(Integer metadataEntityId);

    /**
     * 获取标签id集合
     *
     * @param metadataEntityId
     * @return
     */
    List<Integer> getLabelIdList(Integer metadataEntityId);

}
