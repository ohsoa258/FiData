package com.fisk.datagovernance.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.datasecurity.UserGroupAssignmentPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Mapper
public interface UserGroupAssignmentMapper extends FKBaseMapper<UserGroupAssignmentPO> {
	
}
