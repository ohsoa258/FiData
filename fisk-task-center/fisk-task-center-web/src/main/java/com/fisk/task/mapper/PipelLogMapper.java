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

    @Select("select * from tb_pipel_log where pipel_id=#{pipelLog.pipelId} and del_flag = 1 and create_time >= DATE_SUB( CURDATE( ), INTERVAL #{pipelLog.lookday} DAY ) order by create_time desc")
    List<PipelLogVO> getPipelLogs(@Param("pipelLog") PipelLogVO pipelLog);

    @Update("update tb_pipel_log set del_flag = 0 where pipel_trace_id=#{pipelTraceId} and type = #{type} and del_flag=1")
    void updateByPipelTraceId(@Param("pipelTraceId")String pipelTraceId,@Param("type") int type);

    List<LogStatisticsVO> getStatisticsLog(@Param("lookday") int lookday, @Param("pipelId") int pipelId, @Param("result") String result);
}
