package com.fisk.datafactory.dto.taskschedule;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TaskCronDTO {
    @ApiModelProperty(value = "dto")
    public DataAccessIdDTO dto;
    @ApiModelProperty(value = "编码")
    public ResultEnum code;
    @ApiModelProperty(value = "cronNextTime")
    public String cronNextTime;
    @ApiModelProperty(value = "标记")
    public int flag;
}
