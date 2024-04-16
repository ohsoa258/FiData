package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.BusinessCategoryAssignmentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-04-11 16:45:35
 */
@Mapper
public interface BusinessCategoryAssignmentMapper extends BaseMapper<BusinessCategoryAssignmentPO> {

    List<Integer> getCategoryIds(@Param("roleIds") List<Integer> roleIds);
}
