package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.dto.modelAndIndex.ModelAndIndexMappingDTO;
import com.fisk.datamanagement.entity.FactTreePOs;

import java.util.List;

public interface FactTreeListExtendedfieldsService extends IService<FactTreePOs> {

    List<FactTreePOs> addFactTreeListExtendedfields(String pid);

    /**
     * 关联数仓表字段和指标标准（事实表字段 指标所属）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    Object setMetricBelongsByModelField(List<ModelAndIndexMappingDTO> dtos);
}
