package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.ApitreelistHistoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-09-25 16:13:25
 */
@Mapper
public interface ApitreelistHistoryMapper extends BaseMapper<ApitreelistHistoryPO> {

    @Select("select * from tb_apitreelist_history where history_id = #{historyId} and del_flag = 1")
    List<ApitreelistHistoryPO> selectHistoryId(@Param("historyId") String historyId);
}
