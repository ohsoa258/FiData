package com.fisk.taskfactory.dto;

import com.fisk.common.response.ResultEnum;
import com.fisk.taskfactory.dto.dataaccess.DataAccessIdDTO;
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
