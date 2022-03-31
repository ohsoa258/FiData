package com.fisk.datagovernance.mapper.datasecurity;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.datasecurity.UserGroupInfoPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Mapper
public interface UserGroupInfoMapper extends FKBaseMapper<UserGroupInfoPO> {
	
}
