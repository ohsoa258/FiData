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
}
