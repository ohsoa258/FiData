package com.fisk.task.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.task.vo.WsMessageLogVO;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class MessageLogQuery {
    public Long userId;
    public Page<WsMessageLogVO> page;
}
