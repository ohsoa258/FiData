package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.fact.FactAssociationDTO;
import com.fisk.datamodel.entity.FactPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface FactMapper extends FKBaseMapper<FactPO> {

    FactAssociationDTO getFactDetail(@Param("id") int id);

}
