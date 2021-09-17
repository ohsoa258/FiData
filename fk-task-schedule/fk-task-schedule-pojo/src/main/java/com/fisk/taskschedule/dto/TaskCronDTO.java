package com.fisk.taskschedule.dto;

import com.fisk.common.response.ResultEnum;
import com.fisk.taskschedule.dto.dataaccess.DataAccessIdDTO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TaskCronDTO {
    public DataAccessIdDTO dto;
    public ResultEnum code;
    public String cronNextTime;
    public int flag;
}
