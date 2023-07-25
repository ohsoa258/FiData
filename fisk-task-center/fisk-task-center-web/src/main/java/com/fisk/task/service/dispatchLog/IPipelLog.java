package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.statistics.PipelLineDetailDTO;
import com.fisk.task.vo.statistics.*;
import com.fisk.task.dto.dispatchlog.LogStatisticsForChartVO;
import com.fisk.task.dto.dispatchlog.PipelLogVO;
import com.fisk.task.dto.dispatchlog.PipelMergeLog;
import com.fisk.task.entity.PipelLogPO;

import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
public interface IPipelLog extends IService<PipelLogPO> {

    void savePipelLog(String pipelTraceId, Map<Integer, Object> map, String pipelId);

    List<PipelMergeLog> getPipelLogVos(PipelLogVO pipelLog);

    LogStatisticsForChartVO getLogStatisticsForChart(PipelLogVO pipelLog);

    ResultEntity<String> getPipelIdByTraceId(String pipelTraceId);

    StatisticsVO getLogStatistics(Integer lookday);

    List<GanttChartVO> getGanttChart();

    List<TopRunningTimeVO> getTopRunningTime(Integer lookday);

    List<FaildStatisticsVO> getFaildStatistics(Integer lookday);

    List<LineChartVO> getLineChart(Integer lookday);

    List<DetailLineChartVO> getDetailLineChart(String workflowName, Integer lookday);

    Page<PipelLineDetailVO> getPipelLineDetailLog(PipelLineDetailDTO dto);
    List<PipelLineDetailVO> getDetailLog();
}
