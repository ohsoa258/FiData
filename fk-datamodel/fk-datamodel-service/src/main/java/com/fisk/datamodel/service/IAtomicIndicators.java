package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDTO;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDetailDTO;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsQueryDTO;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsResultDTO;

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

    /**
     * 分页获取原子指标列表
     * @param dto
     * @return
     */
    Page<AtomicIndicatorsResultDTO> getAtomicIndicatorList(AtomicIndicatorsQueryDTO dto);

}
