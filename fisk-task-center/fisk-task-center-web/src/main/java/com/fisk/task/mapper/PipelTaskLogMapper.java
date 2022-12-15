package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.entity.PipelTaskLogPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelTaskLogMapper extends FKBaseMapper<PipelTaskLogPO> {


    List<PipelTaskLogVO> getByTaskId(@Param("taskId") String taskId, @Param("jobTraceId") String jobTraceId);

    @Update("update tb_pipel_task_log set del_flag = 0 where task_trace_id=#{pipelJobId} and type = #{type} and del_flag=1")
    void updateByPipelTraceId(@Param("pipelJobId")String pipelJobId,@Param("type") int type);
}
