package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.entity.DimensionAttributePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author JianWenYang
 */
@Mapper
public interface DimensionAttributeMapper extends FKBaseMapper<DimensionAttributePO> {
}
