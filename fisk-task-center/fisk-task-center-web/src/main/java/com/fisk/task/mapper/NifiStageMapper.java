package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.entity.NifiStagePO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface NifiStageMapper extends FKBaseMapper<NifiStagePO> {

    /**
     * 根据组件id删除数据(逻辑删除)
     *
     * @param componentId 组件id
     * @return 返回值
     */
    @Update("update tb_nifi_stage set del_flag=0 where component_id=#{componentId} and del_flag=1")
    Long deleteByComponentId(@Param("componentId") int componentId);

    /**
     * updateByComponentId
     *
     * @param componentId 组件id
     * @return 返回值
     */
    @Update("update tb_nifi_stage set query_phase=3,transition_phase=3,insert_phase=3 where component_id=#{componentId} and del_flag=1")
    Long updateByComponentId(@Param("componentId") int componentId);
}
