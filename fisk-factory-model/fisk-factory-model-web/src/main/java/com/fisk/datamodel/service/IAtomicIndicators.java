package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.*;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IAtomicIndicators {

    /**
     * 添加原子指标
     * @param dto
     * @return
     */
    ResultEnum addAtomicIndicators(List<AtomicIndicatorsDTO> dto);

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

    /**
     * 获取业务域id获取指标下拉列表
     * @param businessId
     * @return
     */
    List<AtomicIndicatorDropListDTO> atomicIndicatorDropList(int businessId);

    /**
     * 根据事实表id推送所有原子指标
     * @param factIds
     * @return
     */
    List<AtomicIndicatorFactDTO> atomicIndicatorPush(List<Integer> factIds);

    /**
     * 根据事实表id,获取分析指标SQL
     *
     * @param factId
     * @return
     */
    ResultEntity<String> getAnalysisIndexSql(int factId);
}
