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
     *
     */
    public long id;
    /**
     *
     */
    public long pid;
}
