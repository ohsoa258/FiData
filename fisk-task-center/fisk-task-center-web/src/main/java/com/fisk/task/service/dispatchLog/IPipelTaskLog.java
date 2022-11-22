package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskMergeLogVO;
import com.fisk.task.entity.PipelTaskLogPO;


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
    public void savePipelTaskLog(String jobTraceId, String pipelTaskTraceId, Map<Integer, Object> map, String taskId, String tableId, int tableType);

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


}
