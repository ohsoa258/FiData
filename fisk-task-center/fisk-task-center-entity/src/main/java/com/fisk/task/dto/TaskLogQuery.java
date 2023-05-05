package com.fisk.task.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.task.vo.TaskLogVO;
import com.fisk.task.vo.WsMessageLogVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class TaskLogQuery {
    @ApiModelProperty(value = "用户Id")
    public Long userId;
    @ApiModelProperty(value = "详细信息")
    public String details;
    @ApiModelProperty(value = "分页")
    public Page<TaskLogVO> page;
}
