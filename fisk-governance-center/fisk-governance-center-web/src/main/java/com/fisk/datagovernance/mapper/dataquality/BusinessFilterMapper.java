package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗mapper
 * @date 2022/3/23 12:42
 */
@Mapper
public interface BusinessFilterMapper extends FKBaseMapper<BusinessFilterPO> {
}
