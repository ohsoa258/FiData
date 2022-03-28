package com.fisk.datagovernance.mapper.datasecurity;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.datasecurity.DatamaskingConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:48
 */
@Mapper
public interface DatamaskingConfigMapper extends FKBaseMapper<DatamaskingConfigPO> {
}
