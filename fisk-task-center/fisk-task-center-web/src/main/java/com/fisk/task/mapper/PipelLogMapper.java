package com.fisk.task.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.datalogging.PipelTotalDTO;
import com.fisk.datamanagement.dto.datalogging.PipelWeekDTO;
import com.fisk.task.dto.dispatchlog.LogStatisticsVO;
import com.fisk.task.dto.dispatchlog.PipelLogVO;
import com.fisk.task.dto.dispatchlog.PipelMergeLog;
import com.fisk.task.dto.statistics.PipelLineDetailDTO;
import com.fisk.task.entity.PipelLogPO;
import com.fisk.task.vo.statistics.*;
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
    void updateByPipelTraceId(@Param("pipelTraceId") String pipelTraceId, @Param("type") int type);

    List<LogStatisticsVO> getStatisticsLog(@Param("lookday") int lookday, @Param("pipelId") int pipelId, @Param("result") String result);

    @Select("select distinct pipel_id from tb_pipel_log where pipel_trace_id = #{pipelTraceId} and del_flag = 1")
    String getPipelIdByTraceId(@Param("pipelTraceId") String pipelTraceId);

    Integer getPipelineStatisticsLog(@Param("lookday") int lookday, @Param("result") String result,@Param("dispatchDbName")String dispatchDbName);

    List<GanttChartVO> getGanttChart(@Param("dispatchDbName")String dispatchDbName);

    List<TopRunningTimeVO> getTopRunningTime(@Param("lookday") int lookday,
                                             @Param("dispatchDbName")String dispatchDbName);

    List<FaildStatisticsVO> getFaildStatistics(@Param("lookday") int lookday,
                                               @Param("dispatchDbName")String dispatchDbName);

    List<LineChartVO> getLineChart(@Param("lookday") int lookday);

    List<DetailLineChartVO> getDetailLineChart(@Param("workflowName") String workflowName,
                                               @Param("lookday") int lookday,
                                               @Param("dispatchDbName")String dispatchDbName);

    Page<PipelLineDetailVO> getPipelLineDetailLog(Page<PipelLineDetailVO> page,
                                                  @Param("dto") PipelLineDetailDTO dto,
                                                  @Param("dispatchDbName")String dispatchDbName);
    List<PipelLineDetailVO> getDetailLog(@Param("dispatchDbName")String dispatchDbName);

    List<PipelMergeLog> getPipelLogVos(@Param("dto") PipelLogVO dto);
    PipelMergeLog getLastPipelLog(@Param("pipelId")String pipelId);

    PipelTotalDTO getPipelTotals();
    List<PipelWeekDTO> getPipelWeek();
}
