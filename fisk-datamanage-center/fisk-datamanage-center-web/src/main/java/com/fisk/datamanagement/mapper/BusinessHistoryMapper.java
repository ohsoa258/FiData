package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.BusinessHistoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-03-01 14:39:17
 */
@Mapper
public interface BusinessHistoryMapper extends BaseMapper<BusinessHistoryPO> {

    @Select("select history_id from tb_business_history where targetinfo_id = #{id} and del_flag = 1 ORDER BY create_time DESC")
    List<String> getHistoryId(@Param("id") Integer id);
}
