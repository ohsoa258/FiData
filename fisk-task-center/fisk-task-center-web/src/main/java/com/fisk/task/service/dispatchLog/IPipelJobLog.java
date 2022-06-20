package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.entity.PipelJobLogPO;

import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
public interface IPipelJobLog extends IService<PipelJobLogPO> {


    /**
     * 保存日志
     *
     * @param pipelTraceId 管道的traceID
     * @param map          需要添加的参数
     * @param pipelId      管道id
     * @param jobTraceId
     * @param componentId
     * @return
     */
    public void savePipelLogAndJobLog(String pipelTraceId, Map<Integer, Object> map, String pipelId, String jobTraceId, String componentId);

    /**
     * 获取日志
     *
     * @param pipelTraceId 管道的traceID
     * @param componentId
     * @return
     */
    PipelJobLogPO getByPipelTraceId(String pipelTraceId, Long componentId);

    /**
     * 获取日志
     *
     * @param pipelJobLogs pipelJobLogs
     * @return
     */
    List<PipelJobLogVO> getPipelJobLogVos(List<PipelJobLogVO> pipelJobLogs);
    /**
     * 获取日志
     *
     * @param dto dto
     * @return
     */
    void exceptionHandlingLog(DispatchExceptionHandlingDTO dto);


}
