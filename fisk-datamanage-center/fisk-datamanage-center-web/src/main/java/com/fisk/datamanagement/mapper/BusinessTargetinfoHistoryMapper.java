package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.entity.BusinessTargetinfoHistoryPO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-03-01 14:35:45
 */
@Mapper
public interface BusinessTargetinfoHistoryMapper extends BaseMapper<BusinessTargetinfoHistoryPO> {

    @Select("select * from tb_business_targetinfo_history where history_id = #{historyId} and del_flag = 1 ")
    BusinessTargetinfoHistoryPO selectClassification(@Param("historyId") String historyId);
}
