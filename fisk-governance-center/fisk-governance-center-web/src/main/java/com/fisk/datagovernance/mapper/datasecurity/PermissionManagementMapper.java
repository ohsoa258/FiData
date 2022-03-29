package com.fisk.datagovernance.mapper.datasecurity;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.datasecurity.PermissionManagementPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-29 17:59:34
 */
@Mapper
public interface PermissionManagementMapper extends FKBaseMapper<PermissionManagementPO> {
	
}
