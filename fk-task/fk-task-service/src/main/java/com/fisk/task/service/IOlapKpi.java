package com.fisk.task.service;

import com.fisk.task.entity.OlapDimensionPO;
import com.fisk.task.entity.OlapKpiPO;

import java.util.List;

/**
 * 指标
 * @author JinXingWang
 */
public interface IOlapKpi {
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
    boolean batchAdd(List<OlapKpiPO> pos);
}
