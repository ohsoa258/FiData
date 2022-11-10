package com.fisk.task.service.dispatchLog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.dto.dispatchlog.PipelStageLogVO;
import com.fisk.task.entity.PipelStageLogPO;

import java.util.List;
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

    /**
     * 拿这个taskid,根据时间排序,taskTraceId分组,找到最近的三个taskTraceId
     *
     * @param taskId
     * @return
     */
    public List<PipelStageLogVO> getPipelStageLogVos(String taskId);
}
