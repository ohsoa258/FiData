package com.fisk.task.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.LogStatisticsVO;
import com.fisk.task.dto.dispatchlog.PipelLogVO;
import com.fisk.task.entity.PipelLogPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelLogMapper extends FKBaseMapper<PipelLogPO> {


    List<PipelLogVO> getPipelLogs(@Param("pipelLog") PipelLogVO pipelLog);

    @Update("update tb_pipel_log set del_flag = 0 where pipel_trace_id=#{pipelTraceId} and type = #{type} and del_flag=1")
    void updateByPipelTraceId(@Param("pipelTraceId")String pipelTraceId,@Param("type") int type);

    List<LogStatisticsVO> getStatisticsLog(@Param("lookday") int lookday, @Param("pipelId") int pipelId, @Param("result") String result);

    @Select("select distinct pipel_id from tb_pipel_log where pipel_trace_id = #{pipelTraceId} and del_flag = 1")
    String getPipelIdByTraceId(@Param("pipelTraceId") String pipelTraceId);
}
