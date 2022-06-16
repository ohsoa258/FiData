package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.entity.PipelStageLogPO;

import java.util.Map;

/**
 * @author cfk
 */
public interface IPipelStageLog extends IService<PipelStageLogPO> {
    /**
     * 保存日志
     *
     * @param stateTraceId
     * @param pipelTaskTraceId
     * @param map
     * @return
     */
    public void savePipelTaskStageLog(String stateTraceId, String pipelTaskTraceId, Map<Integer, Object> map);
}
