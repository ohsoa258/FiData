package com.fisk.dataaccess.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.entity.ApiConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Mapper
public interface ApiConfigMapper extends FKBaseMapper<ApiConfigPO> {
	
}
