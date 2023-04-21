package com.fisk.datafactory.dto.tasknifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class PortRequestParamDTO {

    /**
     * 1: 管道;  2: 管道内部
     */
    @ApiModelProperty(value = "1: 管道;  2: 管道内部")
    public int flag;
    /**
     *  workflow_id
     */
    @ApiModelProperty(value = "id")
    public String id;
    /**
     * pid
     */
    @ApiModelProperty(value = "pid")
    public String pid;
}
