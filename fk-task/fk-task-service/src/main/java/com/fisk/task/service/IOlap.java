package com.fisk.task.service;

import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.entity.OlapPO;

import java.util.List;

/**
 * 建模
 * @author JinXingWang
 */
public interface IOlap {
    /**
     * 生成创建模型sql
     * @param businessAreaId 业务域Id
     * @param dto 业务域维度建模
     * @return
     */
    List<OlapPO> build(int businessAreaId, BusinessAreaGetDataDTO dto);
}
