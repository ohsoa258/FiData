package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDetailDTO;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsQueryDTO;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsResultDTO;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface AtomicIndicatorsMapper extends FKBaseMapper<AtomicIndicatorsPO> {

    /**
     * 获取原子指标详情
     * @param id
     * @return
     */
    AtomicIndicatorsDetailDTO AtomicIndicatorsDetailDTO(@Param("id") int id);

    /**
     * 分页获取原子指标列表
     * @param page
     * @param query
     * @return 查询结果
     */
    Page<AtomicIndicatorsResultDTO> queryList(Page<AtomicIndicatorsResultDTO> page, @Param("query") AtomicIndicatorsQueryDTO query);

}
