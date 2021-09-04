package com.fisk.task.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.task.entity.OlapDimensionPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author JinXingWang
 */
public interface OlapDimensionMapper extends FKBaseMapper<OlapDimensionPO> {
    /**
     * 根据业务域主键删除数据(逻辑删除)
     * @param businessId 业务域id
     * @return 返回值
     */
    @Update("update tb_task_olap_dimension set del_flag=0 where business_area_id=#{businessId} and del_flag=1")
    Long deleteByBusinessId(@Param("businessId")int businessId);


}
