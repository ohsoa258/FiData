package com.fisk.task.entity.dto;

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
