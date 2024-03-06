package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.entity.FacttreelistHistoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-03-01 14:37:30
 */
@Mapper
public interface FacttreelistHistoryMapper extends BaseMapper<FacttreelistHistoryPO> {

    @Select("select * from tb_facttreelist_history where history_id = #{historyId} and del_flag = 1")
    List<FacttreelistHistoryPO> selectHistoryId(@Param("historyId") String historyId);
}
