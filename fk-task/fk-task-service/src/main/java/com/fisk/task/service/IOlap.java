package com.fisk.task.service;

import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;

import java.util.List;

/**
 * 建模
 * @author JinXingWang
 */
public interface IOlap {
    /**
     * 生成创建模型sql
     * @return
     */
    boolean build(List<ModelMetaDataDTO> modelMetaDataDTOS);
}
