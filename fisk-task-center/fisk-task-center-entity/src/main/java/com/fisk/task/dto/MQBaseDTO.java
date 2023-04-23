package com.fisk.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Data
public class MQBaseDTO {
    public Long userId;
    public LocalDateTime sendTime;
    public Long logId;
    public String traceId;
    public String spanId;
    /**
     * 报错信息
     */
    public String msg;
}
