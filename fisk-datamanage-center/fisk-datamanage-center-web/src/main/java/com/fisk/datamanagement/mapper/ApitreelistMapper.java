package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.ApitreePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-09-24 14:18:07
 */
@Mapper
public interface ApitreelistMapper extends BaseMapper<ApitreePO> {

    @Select("select * from tb_apitreelist where pid = #{pid} and del_flag = 1")
    List<ApitreePO> selectParentpIds(@Param("pid") String pid);
}
