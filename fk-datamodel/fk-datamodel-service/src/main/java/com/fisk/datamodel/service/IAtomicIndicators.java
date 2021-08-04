package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDTO;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDetailDTO;

/**
 * @author JianWenYang
 */
public interface IAtomicIndicators {

    /**
     * 添加原子指标
     * @param dto
     * @return
     */
    ResultEnum addAtomicIndicators(AtomicIndicatorsDTO dto);

    /**
     * 根据id删除原子指标
     * @param id
     * @return
     */
    ResultEnum deleteAtomicIndicators(int id);

    /**
     * 根据id获取原子指标详情
     * @param id
     * @return
     */
    AtomicIndicatorsDetailDTO getAtomicIndicatorDetails(int id);

    /**
     * 修改原子指标
     * @param dto
     * @return
     */
    ResultEnum updateAtomicIndicatorDetails(AtomicIndicatorsDTO dto);

}
