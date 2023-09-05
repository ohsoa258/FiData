package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.dto.dispatchlog.PipelJobMergeLogVO;
import com.fisk.task.entity.PipelJobLogPO;

import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
public interface IPipelJobLog extends IService<PipelJobLogPO> {


    /**
     * 保存管道日志
     *
     * @param pipelTraceId 管道的traceID
     * @param map          需要添加的参数
     * @param pipelId      管道id
     * @return
     */
    public void savePipelLog(String pipelTraceId, Map<Integer, Object> map, String pipelId);

    /**
     * 保存job日志
     *
     * @param pipelTraceId 管道的traceID
     * @param map          需要添加的参数
     * @param pipelId      管道id
     * @param jobTraceId
     * @param componentId
     * @return
     */
    public void savePipelJobLog(String pipelTraceId, Map<Integer, Object> map, String pipelId, String jobTraceId, String componentId);

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
    List<PipelJobMergeLogVO> getPipelJobLogVos1(List<PipelJobLogVO> pipelJobLogs);

    List<PipelJobMergeLogVO> getPipelJobLogVos(String pipelTraceId);
    /**
     * 获取日志
     *
     * @param dto dto
     * @return
     */
    void exceptionHandlingLog(DispatchExceptionHandlingDTO dto) throws InterruptedException;


}
