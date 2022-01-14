package com.fisk.dataservice.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.FieldConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * api 字段 mapper
 *
 * @author dick
 */
@Mapper
public interface ApiFieldMapper extends FKBaseMapper<FieldConfigPO>
{

}
