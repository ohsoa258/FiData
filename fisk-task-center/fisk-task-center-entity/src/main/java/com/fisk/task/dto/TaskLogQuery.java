package com.fisk.task.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.task.vo.TaskLogVO;
import com.fisk.task.vo.WsMessageLogVO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class TaskLogQuery {
    public Long userId;
    public String details;
    public Page<TaskLogVO> page;
}
