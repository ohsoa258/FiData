package com.fisk.task.vo;

import com.fisk.common.enums.task.MessageStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Data
public class WsMessageLogVO {
    public String msg;
    public LocalDateTime createTime;
    public MessageStatusEnum status;
    public int id;
}
