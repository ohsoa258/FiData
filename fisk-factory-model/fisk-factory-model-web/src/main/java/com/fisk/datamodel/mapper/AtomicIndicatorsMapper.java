package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsQueryDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsResultDTO;
import com.fisk.datamodel.entity.IndicatorsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface AtomicIndicatorsMapper extends FKBaseMapper<IndicatorsPO> {

    /**
     * 分页获取原子指标列表
     * @param page
     * @param query
     * @return 查询结果
     */
    Page<AtomicIndicatorsResultDTO> queryList(Page<AtomicIndicatorsResultDTO> page, @Param("query") AtomicIndicatorsQueryDTO query);

}
