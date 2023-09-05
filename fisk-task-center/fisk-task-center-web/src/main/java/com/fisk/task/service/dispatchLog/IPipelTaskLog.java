package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.dispatchlog.DataServiceTableLogQueryVO;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskMergeLogVO;
import com.fisk.task.dto.query.DataServiceTableLogQueryDTO;
import com.fisk.task.dto.tableservice.TableServiceDetailDTO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.vo.tableservice.*;


import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
public interface IPipelTaskLog extends IService<PipelTaskLogPO> {


    /**
     * 保存日志
     *
     * @param jobTraceId
     * @param pipelTaskTraceId
     * @param map
     * @param taskId
     * @return
     */
    public void savePipelTaskLog(String pipelTraceId, String jobTraceId, String pipelTaskTraceId, Map<Integer, Object> map, String taskId, String tableId, int tableType);

    /**
     * 获取日志
     *
     * @param pipelJobTraceId
     * @param taskId
     * @return
     */
    PipelTaskLogPO getByPipelJobTraceId(String pipelJobTraceId, Long taskId);

    /**
     * 获取task日志
     *
     * @param pipelTaskLogs
     * @return
     */
    List<PipelTaskMergeLogVO> getPipelTaskLogVos(List<PipelTaskLogVO> pipelTaskLogs);

    List<PipelTaskMergeLogVO> getPipelTaskLogVos1(String JobTraceId);

    /**
     * 获取数据服务表服务同步日志
     *
     * @param dto
     * @return
     */
    ResultEntity<DataServiceTableLogQueryVO> getDataServiceTableLogVos(DataServiceTableLogQueryDTO dto);
    /**
     * 更新管道任务日志
     * @param pipelTaskTraceId
     */
    void updatePipelTaskLog(String pipelTaskTraceId);
    /**
     * 获取图表的日志统计信息
     * @param lookday
     * @return
     */
    TableStatisticsVO getLogStatistics(Integer lookday);
    /**
     * 获取日志统计页面甘特图
     * @return
     */
    List<TableGanttChartVO> getGanttChart();
    /**
     * 获取表服务运行时长TOP20
     * @param lookday
     * @return
     */
    List<TableTopRunningTimeVO> getTopRunningTime(Integer lookday);

    /**
     * 获取失败统计图
     * @param lookday
     * @return
     */
    List<TableFaildStatisticsVO> getFaildStatistics(Integer lookday);

    /**
     * 获取表服务运行状态趋势图
     * @param lookday
     * @return
     */
    List<TableLineChartVO> getLineChart(Integer lookday);

    /**
     * 获取表服务运行时长TOP详情
     * @param tableName
     * @param lookday
     * @return
     */
    List<TableServiceLineChartVO> getDetailLineChart(String tableName, Integer lookday);

    /**
     * 获取表服务日志详情页
     * @param dto
     * @return
     */
    Page<TableServiceDetailVO> getTableServiceDetailLog(TableServiceDetailDTO dto);
    /**
     * 获取日志详情
     *
     * @return
     */
    List<TableServiceDetailVO> getDetailLog();
}
