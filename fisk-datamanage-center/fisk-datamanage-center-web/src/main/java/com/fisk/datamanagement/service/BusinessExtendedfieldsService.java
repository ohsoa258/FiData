package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.dto.modelAndIndex.ModelAndIndexMappingDTO;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;

import java.util.List;

public interface BusinessExtendedfieldsService extends IService<BusinessExtendedfieldsPO> {
    /**
     * 展示维度数据
     * @param
     * @return
     */
    List<BusinessExtendedfieldsPO> addBusinessExtendedfields(String indexid);

    /**
     * 关联数仓表字段和指标标准（维度表字段 指标粒度）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    Object setMetricGranularityByModelField(List<ModelAndIndexMappingDTO> dtos);

}
