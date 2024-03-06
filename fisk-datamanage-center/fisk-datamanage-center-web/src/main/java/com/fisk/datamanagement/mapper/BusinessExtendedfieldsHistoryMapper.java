package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsHistoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-03-01 14:32:54
 */
@Mapper
public interface BusinessExtendedfieldsHistoryMapper extends BaseMapper<BusinessExtendedfieldsHistoryPO> {

    @Select("select * from tb_business_extendedfields_history where history_id = #{historyId} and del_flag = 1")
    List<BusinessExtendedfieldsHistoryPO> selectHistoryId(@Param("historyId") String historyId);
}
