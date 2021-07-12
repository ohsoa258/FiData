package com.fisk.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.system.dto.RoleInfoDTO;
import com.fisk.system.entity.RoleInfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface RoleInfoMapper extends FKBaseMapper<RoleInfoPO> {

    /**
     * 获取权限下所有数据源连接
     *
     * @return 查询结果
     */
    Page<RoleInfoDTO> listRoleData(Page page);

}
