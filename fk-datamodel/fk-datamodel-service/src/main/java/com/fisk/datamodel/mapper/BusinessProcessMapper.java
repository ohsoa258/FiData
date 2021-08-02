package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.businessprocess.BusinessProcessAssociationDTO;
import com.fisk.datamodel.entity.BusinessProcessPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface BusinessProcessMapper extends FKBaseMapper<BusinessProcessPO> {

    BusinessProcessAssociationDTO getBusinessProcessDetail(@Param("id") int id);

}
