package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsListDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsQueryDTO;

/**
 * @author JianWenYang
 */
public interface IDerivedIndicators {

    /**
     * 获取派生指标列表
     * @param dto
     * @return
     */
    Page<DerivedIndicatorsListDTO> getDerivedIndicatorsList(DerivedIndicatorsQueryDTO dto);

    /**
     * 删除派生指标
     * @param id
     * @return
     */
    ResultEnum deleteDerivedIndicators(long id);

    /**
     * 添加派生指标
     * @param dto
     * @return
     */
    ResultEnum addDerivedIndicators(DerivedIndicatorsDTO dto);

    /**
     * 获取派生指标详情
     * @param id
     * @return
     */
    DerivedIndicatorsDTO getDerivedIndicators(long id);

    /**
     * 修改派生指标
     * @param dto
     * @return
     */
    ResultEnum updateDerivedIndicators(DerivedIndicatorsDTO dto);

}
