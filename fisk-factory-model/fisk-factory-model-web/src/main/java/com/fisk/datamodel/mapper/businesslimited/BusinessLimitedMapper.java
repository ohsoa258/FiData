package com.fisk.datamodel.mapper.businesslimited;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.businesslimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businesslimited.BusinessLimitedQueryDTO;
import com.fisk.datamodel.entity.businesslimited.BusinessLimitedPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
/**
 * @author cfk
 */
@Mapper
public interface BusinessLimitedMapper  extends FKBaseMapper<BusinessLimitedPO> {
    /**
     * 获取业务限定列表
     * @param query
     * @param page
     * @return
     */
    Page<BusinessLimitedDTO>  queryList(Page<BusinessLimitedDTO> page,@Param("query") BusinessLimitedQueryDTO query);
    /**
     * 更新业务限定
     * @param businessLimitedDto
     * @return
     */
    int insertBusinessLimited(@Param("businessLimitedDto")BusinessLimitedDTO businessLimitedDto);
}
