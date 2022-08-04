package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.entity.PipelLogPO;

import java.util.Map;

/**
 * @author cfk
 */
public interface IPipelLog extends IService<PipelLogPO> {

    void savePipelLog(String pipelTraceId, Map<Integer, Object> map, String pipelId);
}
