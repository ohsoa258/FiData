package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiResultPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗结果
 * @date 2022/10/8 16:58
 */
@Mapper
public interface BusinessFilterApiResultMapper extends FKBaseMapper<BusinessFilterApiResultPO> {
}
