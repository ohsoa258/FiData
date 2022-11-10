package com.fisk.task.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.entity.PipelineTableLogPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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
    @Update("update tb_pipeline_table_log set state=3 where component_id=#{componentId} and table_id=#{tableId} and table_type=#{tableType} and del_flag=1")
    Long updateByComponentId(@Param("componentId") Integer componentId, @Param("tableId") Integer tableId, @Param("tableType") Integer tableType);

    List<PipelineTableLogVO> getPipelineTableLogs(@Param("tableId") Integer tableId, @Param("tableType") Integer tableType, @Param("keyword") String keyword);

    /**
     * 根据组件id获取数据
     *
     * @param componentId 组件id
     * @return 返回值
     */
    @Select("select * from tb_pipeline_table_log where component_id=#{componentId} and del_flag=1 order by create_time desc limit 1")
    PipelineTableLogDTO selectByComponentId(@Param("componentId") Long componentId);
}
