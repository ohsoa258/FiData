package com.fisk.task.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.task.entity.PipelineTableLogPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author cfk
 */
public interface PipelineTableLogMapper extends FKBaseMapper<PipelineTableLogPO> {

    /**
     * 根据组件id删除数据(逻辑删除)
     *
     * @param componentId 组件id
     * @return 返回值
     */
    @Update("update tb_pipeline_table_log set del_flag=0 where component_id=#{componentId} and del_flag=1")
    Long deleteByComponentId(@Param("componentId") int componentId);

    /**
     * 根据组件id删除数据(逻辑删除)
     *
     * @param componentId 组件id
     * @return 返回值
     */
    @Update("update tb_pipeline_table_log set state=3 where component_id=#{componentId} and del_flag=1")
    Long updateByComponentId(@Param("componentId") int componentId);
}
