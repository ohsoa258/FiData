package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParmPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗参数
 * @date 2022/10/8 16:58
 */
@Mapper
public interface BusinessFilterApiParmMapper extends FKBaseMapper<BusinessFilterApiParmPO> {
}
