package com.fisk.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.system.dto.RoleInfoDTO;
import com.fisk.system.entity.RoleInfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface RoleInfoMapper extends FKBaseMapper<RoleInfoPO> {

    /**
     * 获取所有角色列表
     *
     * @return 查询结果
     */
    List<RoleInfoDTO> roleList();

}
