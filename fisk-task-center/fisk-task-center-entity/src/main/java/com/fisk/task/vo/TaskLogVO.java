package com.fisk.task.vo;

import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.task.dto.task.TaskLogDTO;
import com.fisk.task.enums.TaskStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class TaskLogVO extends BasePO {
    /**
     * @param taskName 取排序后最新的那个,任务描述
     */
    public String taskName;
    /**
     * @param taskQueue 取排序后最新的那个,任务topic
     */
    public String taskQueue;
    /**
     * @param taskStatus 取排序后最新的那个,任务状态
     */
    public TaskStatusEnum taskStatus;
    /**
     * @param traceId 取排序后最新的那个,任务traceId
     */
    public String traceId;
    /**
     * @param taskLogs 取排序后最新的那个,此traceId下所有的任务
     */
    public List<TaskLogDTO> taskLogs;


}
