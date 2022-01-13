package com.fisk.dataservice.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.BuiltinParmPO;
import com.fisk.dataservice.entity.ParmConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * api内置参数 mapper
 *
 * @author dick
 */
@Mapper
public interface ApiBuiltinParmMapper extends FKBaseMapper<BuiltinParmPO>
{

}
