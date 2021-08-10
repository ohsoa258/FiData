package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedQueryDTO;
import com.fisk.datamodel.entity.BusinessLimitedPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
/**
 * @author cfk
 */
@Mapper
public interface BusinessLimitedMapper  extends FKBaseMapper<BusinessLimitedPO> {

    Page<BusinessLimitedDTO>  queryList(Page<BusinessLimitedDTO> page,@Param("query") BusinessLimitedQueryDTO query);
}
