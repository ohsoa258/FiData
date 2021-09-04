package com.fisk.taskschedule.dto;

import com.fisk.common.response.ResultEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TaskCronDTO {
    public ResultEnum code;
    public String cronNextTime;
}
