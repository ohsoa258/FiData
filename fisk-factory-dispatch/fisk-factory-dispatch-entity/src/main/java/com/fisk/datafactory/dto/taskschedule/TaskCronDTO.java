package com.fisk.datafactory.dto.taskschedule;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
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
