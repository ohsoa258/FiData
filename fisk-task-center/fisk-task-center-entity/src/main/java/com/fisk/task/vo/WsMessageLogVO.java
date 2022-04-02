package com.fisk.task.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.enums.task.MessageLevelEnum;
import com.fisk.common.core.enums.task.MessageStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Data
public class WsMessageLogVO {
    public String msg;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    public MessageLevelEnum level;
    public MessageStatusEnum status;
    public long id;
}
