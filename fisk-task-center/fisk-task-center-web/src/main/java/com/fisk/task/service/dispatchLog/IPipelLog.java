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

    /**
     * 获取管道日志
     *
     * @param pipelLog pipelLog
     * @return 执行结果
     */
    List<PipelMergeLog> getPipelLogVos(PipelLogVO pipelLog);

    /**
     * 获取图表的日志统计信息
     * @param pipelLog
     * @return
     */
    LogStatisticsForChartVO getLogStatisticsForChart(PipelLogVO pipelLog);

    /**
     * 依据pipelTraceId查询pipelId
     * @param pipelTraceId
     * @return
     */
    ResultEntity<String> getPipelIdByTraceId(String pipelTraceId);

    /**
     * 获取图表的日志统计信息
     * @param lookday
     * @return
     */
    StatisticsVO getLogStatistics(Integer lookday);

    /**
     * 获取日志统计页面甘特图
     * @return
     */
    List<GanttChartVO> getGanttChart();

    /**
     * 获取管道运行时长TOP20
     * @param lookday
     * @return
     */
    List<TopRunningTimeVO> getTopRunningTime(Integer lookday);

    /**
     * 获取失败统计图
     * @param lookday
     * @return
     */
    List<FaildStatisticsVO> getFaildStatistics(Integer lookday);

    /**
     * 获取管道运行状态趋势图
     * @param lookday
     * @return
     */
    List<LineChartVO> getLineChart(Integer lookday);

    /**
     * 获取管道运行时长TOP详情
     * @param workflowName
     * @param lookday
     * @return
     */
    List<DetailLineChartVO> getDetailLineChart(String workflowName, Integer lookday);

    /**
     * 获取管道日志详情页
     * @param dto
     * @return
     */
    Page<PipelLineDetailVO> getPipelLineDetailLog(PipelLineDetailDTO dto);
    /**
     * 获取日志详情
     * @return
     */
    List<PipelLineDetailVO> getDetailLog();
}
