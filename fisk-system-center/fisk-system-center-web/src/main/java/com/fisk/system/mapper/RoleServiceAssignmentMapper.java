package com.fisk.system.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.dto.RoleServiceAssignmentDTO;
import com.fisk.system.entity.RoleServiceAssignmentPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface RoleServiceAssignmentMapper extends FKBaseMapper<RoleServiceAssignmentPO> {
    List<RoleServiceAssignmentDTO> getRoleServiceAssignmentDto(@Param("roleId")int roleId);
}
