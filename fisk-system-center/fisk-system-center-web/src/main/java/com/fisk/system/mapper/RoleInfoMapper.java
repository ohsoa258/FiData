package com.fisk.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.roleinfo.RolePageDTO;
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

    /**
     * 获取角色分页数据
     * @param page
     * @param query
     * @return
     */
    Page<RoleInfoDTO> roleList(Page<RoleInfoDTO> page, @Param("query")RolePageDTO query);

}
