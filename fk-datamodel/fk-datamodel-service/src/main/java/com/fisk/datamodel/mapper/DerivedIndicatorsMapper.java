package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsListDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsQueryDTO;
import com.fisk.datamodel.entity.DerivedIndicatorsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface DerivedIndicatorsMapper extends FKBaseMapper<DerivedIndicatorsPO> {
    /**
     * 查询派生指标列表
     * @param page
     * @param dto
     * @return
     */
    Page<DerivedIndicatorsListDTO> queryList(Page<DerivedIndicatorsListDTO> page, @Param("query") DerivedIndicatorsQueryDTO dto);
}
