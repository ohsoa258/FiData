package com.fisk.datafactory.dto.tasknifi;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class PortRequestParamDTO {

    /**
     * 1: 管道;  2: 管道内部
     */
    public int flag;
    /**
     *  workflow_id
     */
    public String id;
    /**
     * pid
     */
    public long pid;
}
