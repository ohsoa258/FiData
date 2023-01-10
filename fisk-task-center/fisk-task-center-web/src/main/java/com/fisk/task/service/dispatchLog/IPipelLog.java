package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
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

    ResultEntity<Object> getPipelIdByTraceId(String pipelTraceId);
}
