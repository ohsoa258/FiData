package com.fisk.task.service;

import com.fisk.task.entity.OlapDimensionPO;

import java.util.List;


/**
 * 建模维度
 * @author JinXingWang
 */
public interface IOlapDimension {
    /**
     * 根据业务域ID删除
     * @param businessAreaId 业务域iD
     * @return
     */
    boolean deleteByBusinessAreaId(int businessAreaId);

    /**
     * 批量添加
     * @param pos
     * @return
     */
    boolean batchAdd(List<OlapDimensionPO> pos);
}
