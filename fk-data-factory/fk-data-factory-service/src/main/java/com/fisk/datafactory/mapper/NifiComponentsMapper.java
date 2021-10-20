package com.fisk.datafactory.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datafactory.entity.NifiComponentsPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Lock
 */
@Mapper
public interface NifiComponentsMapper extends FKBaseMapper<NifiComponentsPO> {
}
