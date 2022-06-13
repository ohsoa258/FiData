package com.fisk.system.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.entity.DataSourcePO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 数据源连接mapper
 *
 * @author dick
 */
@Mapper
public interface DataSourceMapper extends FKBaseMapper<DataSourcePO> {

}
