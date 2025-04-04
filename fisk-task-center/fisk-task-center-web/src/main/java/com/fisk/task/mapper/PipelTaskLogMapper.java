package com.fisk.task.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.task.dto.dispatchlog.DataServiceTableLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskMergeLogVO;
import com.fisk.task.dto.query.DataServiceTableLogQueryDTO;
import com.fisk.task.dto.tableservice.TableServiceDetailDTO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.vo.tableservice.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author cfk
 */
public interface PipelTaskLogMapper extends FKBaseMapper<PipelTaskLogPO> {


    List<PipelTaskLogVO> getByTaskId(@Param("taskId") String taskId, @Param("jobTraceId") String jobTraceId);

    @Update("update tb_pipel_task_log set del_flag = 0 where task_trace_id=#{pipelJobId} and type = #{type} and del_flag=1")
    void updateByPipelTraceId(@Param("pipelJobId")String pipelJobId,@Param("type") int type);
    @Update("update tb_pipel_task_log set msg = #{msg} where task_trace_id=#{taskTraceId} and type = #{type} and del_flag=1")
    void updateMsgByPipelTraceId(@Param("msg") String msg,@Param("taskTraceId")String taskTraceId,@Param("type") int type);
    List<DataServiceTableLogVO> getDataServiceTableLogs(@Param("query") DataServiceTableLogQueryDTO query);

    Integer getTableServerStatisticsLog(@Param("lookday") int lookday, @Param("result") String result);

    List<TableGanttChartVO> getGanttChart(@Param("dataServiceDbName")String dataServiceDbName);

    List<TableTopRunningTimeVO> getTopRunningTime(@Param("lookday") int lookday,
                                                  @Param("dataServiceDbName")String dataServiceDbName);

    List<TableFaildStatisticsVO> getFaildStatistics(@Param("lookday") int lookday,@Param("dataServiceDbName")String dataServiceDbName);

    List<TableLineChartVO> getLineChart(@Param("lookday") int lookday);

    List<TableServiceLineChartVO> getDetailLineChart(@Param("tableName") String tableName,
                                                     @Param("lookday") int lookday,
                                                     @Param("dataServiceDbName")String dataServiceDbName);

    Page<TableServiceDetailVO> getTableServiceDetailLog(Page<TableServiceDetailVO> page,
                                                        @Param("dto") TableServiceDetailDTO dto,
                                                        @Param("dataServiceDbName")String dataServiceDbName);
    List<TableServiceDetailVO> getDetailLog(@Param("dataServiceDbName")String dataServiceDbName);

    List<PipelTaskMergeLogVO> getPipelTaskLogVos(@Param("JobTraceId")String JobTraceId);
}
