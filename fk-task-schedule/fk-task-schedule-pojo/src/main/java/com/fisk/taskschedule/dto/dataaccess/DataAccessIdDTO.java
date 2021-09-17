package com.fisk.taskschedule.dto.dataaccess;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataAccessIdDTO {
    public long appId;
    public long tableId;
    /**
     * Timer driven OR CRON driven
     */
    public String syncMode;
    /**
     * 表达式 OR 秒
     */
    public String expression;
}
