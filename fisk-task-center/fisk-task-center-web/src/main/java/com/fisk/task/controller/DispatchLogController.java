package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.task.dto.dispatchlog.PipelJobLogVO;
import com.fisk.task.dto.dispatchlog.PipelStageLogVO;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cfk
 */
@RestController
@RequestMapping("/dispatchLog")
@Slf4j
public class DispatchLogController {
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelStageLog iPipelStageLog;

    /**
     * 获取管道日志
     *
     * @param pipelJobLogs pipelJobLogs
     * @return 执行结果
     */
    @PostMapping("/getPipelJobLogVos")
    public ResultEntity<List<PipelJobLogVO>> getPipelJobLogVos(@RequestBody List<PipelJobLogVO> pipelJobLogs) {
        ResultEntity<List<PipelJobLogVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelJobLog.getPipelJobLogVos(pipelJobLogs);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    /**
     * 获取任务日志
     *
     * @param pipelTaskLogs pipelTaskLogs
     * @return 执行结果
     */
    @PostMapping("/getPipelTaskLogVos")
    public ResultEntity<List<PipelTaskLogVO>> getPipelTaskLogVos(@RequestBody List<PipelTaskLogVO> pipelTaskLogs) {
        ResultEntity<List<PipelTaskLogVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelTaskLog.getPipelTaskLogVos(pipelTaskLogs);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }

    /**
     * 获取阶段日志
     *
     * @param taskId taskId
     * @return 执行结果
     */
    @PostMapping("/getPipelStageLogVos")
    public ResultEntity<List<PipelStageLogVO>> getPipelStageLogVos(@RequestParam String taskId) {
        ResultEntity<List<PipelStageLogVO>> objectResultEntity = new ResultEntity<>();
        objectResultEntity.data = iPipelStageLog.getPipelStageLogVos(taskId);
        objectResultEntity.code = 0;
        return objectResultEntity;
    }
}
