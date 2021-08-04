package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDetailDTO;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface AtomicIndicatorsMapper extends FKBaseMapper<AtomicIndicatorsPO> {

    AtomicIndicatorsDetailDTO AtomicIndicatorsDetailDTO(@Param("id") int id);

}
