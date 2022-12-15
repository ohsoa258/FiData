package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.entity.PipelJobLogPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelJobLogMapper extends FKBaseMapper<PipelJobLogPO> {

    List<PipelJobLogVO> getPipelJobLogVo(@Param("pipelJobLog") PipelJobLogVO pipelJobLog);

    @Update("update tb_pipel_job_log set del_flag = 0 where job_trace_id=#{pipelJobId} and type = #{type} and del_flag=1")
    void updateByPipelTraceId(@Param("pipelJobId")String pipelJobId,@Param("type") int type);



}
